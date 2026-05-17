# Seckill - 商城秒杀系统

## 慢查询治理 - 优化记录

### 数据环境
- 订单量：1,000,000 条
- 订单明细：2,000,000 条
- MySQL：Docker 8.0
- 表结构：`tb_order`（订单主表） + `tb_order_item`（订单明细表）

---

### 优化一：全文索引替代 LIKE 模糊匹配（2026-05-16）

**问题接口**：`POST /api/orders/search`

**优化前 SQL**：
```sql
AND oi.product_name LIKE CONCAT('%', #{productName}, '%')
```

**优化方案**：
1. 在 `tb_order_item.product_name` 上创建全文索引
2. 将 `LIKE '%keyword%'` 改写为 `MATCH(product_name) AGAINST('keyword' IN BOOLEAN MODE)`

**改动文件**：
- `src/main/resources/mapper/OrderMapper.xml`（第 22、44 行）

**执行计划变化**：
| 阶段 | 原计划 | 优化后 |
|------|--------|--------|
| 扫描方式 | `order_item` 全表扫描（2,000,000 行） | 全文索引倒排查询 |
| 扫描行数 | ~2,000,000 | ~220,000（仅含"蓝牙"的行） |
| Extra | `Using where; Using temporary; Using filesort` | 利用索引定位 |

**量化结果**：
| 指标 | 优化前 | 优化后 | 提升倍数 |
|------|--------|--------|---------|
| 接口耗时 | 2377 ms | 30 ms | **×79** |
| AOP 日志级别 | WARN（慢查询告警） | INFO（正常） | - |

---

### 优化二：覆盖索引消除全表扫描 — GROUP BY 聚合查询（2026-05-16）

**问题接口**：`GET /api/orders/category-stats`

**优化前 SQL**（在 `OrderMapper.xml` 中）：
```sql
SELECT category_id, category_name, `status`,
       COUNT(*) AS order_count,
       SUM(total_amount) AS total_amount
FROM tb_order
GROUP BY category_id, category_name, `status`
ORDER BY category_id, `status`
```

**性能瓶颈**：
- 全表扫描 `tb_order`（~1,000,000 行）
- `Using temporary; Using filesort` — MySQL 使用临时表做分组聚合

**优化方案**：
1. 添加覆盖索引：`idx_category_stats(category_id, category_name, status, total_amount)`
2. 索引包含查询涉及的全部列，无需回表访问
3. B+Tree 索引本身按 `(category_id, category_name, status)` 有序排列，减少排序开销

**执行计划变化**：
| 阶段 | 原计划 | 优化后 |
|------|--------|--------|
| 扫描方式 | `ALL` 全表扫描 | `index` 索引全扫描 |
| 回表 | 需要读取行数据 | `Using index`（完全覆盖，零回表）|
| Extra | `Using temporary; Using filesort` | `Using index; Using temporary; Using filesort` |
| 扫描行数 | ~1,000,000 | ~1,000,000（但索引页更紧凑，IO 更少）|

**量化结果**：
| 指标 | 优化前 | 优化后 | 提升倍数 |
|------|--------|--------|---------|
| 接口耗时 | 621 ms | 141 ms | **×4.4** |
| 扫描方式 | 全表数据页扫描 | 紧凑索引页扫描 | - |
| AOP 日志级别 | INFO（接近阈值） | INFO（正常） | - |

---

### 优化三：游标分页替代 LIMIT offset — 消除深翻页与 COUNT 全表扫描（2026-05-16）

**问题接口**：
- `POST /api/orders/search`（无关键词时）
- `POST /api/orders/filter`
- `POST /api/orders/user`

**问题一：COUNT 全表扫描**

search 和 filter 接口每次查询都执行两条 SQL：
```sql
-- SQL 1: 取当前页数据（有 LIMIT，可提前停止）
SELECT ... LIMIT offset, pageSize;

-- SQL 2: 统计总条数（无 LIMIT，必须扫描全部数据）
SELECT COUNT(DISTINCT o.id) FROM tb_order INNER JOIN tb_order_item ...;
```
当没有 `productName` 过滤条件时，COUNT 需要 JOIN 两张表并扫描所有行（1,000,000 + 2,000,000），这是 search 无关键词耗时 1815ms 的根源。

**问题二：LIMIT offset 深翻页**

```sql
-- 第 10000 页：LIMIT 99990, 10
```
MySQL 无法跳过 offset 行，必须从第 1 行开始数到 99990，offset 越大越慢。

**问题三：Java 内存分页**

`queryUserOrders` 使用 MyBatis-Plus `selectList` 查出该用户全部订单，再用 `subList` 在 Java 内存中手动切页。每次翻页都传全部数据，浪费网络和内存。

**优化方案**：

1. **游标分页**（search、filter）：
   - 去除 `pageNum`，改为 `cursor`（上一页最后一条的 `create_time`）+ `lastId`（tiebreaker）
   - 去除 `LIMIT offset`，改为 `WHERE create_time < #{cursor} OR (create_time = #{cursor} AND id < #{lastId})`
   - 去除 `ORDER BY create_time`，改为 `ORDER BY create_time DESC, id DESC`
   - 游标利用 `idx_create_time` B+Tree 直接定位到目标位置，**每次翻页固定扫描 pageSize 行，与翻页深度无关**
   - B+Tree 定位时间复杂度 O(log n)，翻 1 页和翻 10 万页性能一致

2. **消除 COUNT**：
   - 游标分页不依赖总条数，前端改为"加载更多"或"上一页/下一页"
   - 彻底消除 COUNT 全表扫描

3. **条件性 JOIN**（search）：
   - 无 `productName` 时不 JOIN `tb_order_item`，`DISTINCT` 也随之去除
   - 避免无意义的全量 JOIN

4. **修复内存分页**（user）：
   - `selectList` + `subList` → 自定义 SQL 加 `LIMIT offset, pageSize`
   - 保留 `COUNT`（用户维度数据量小，COUNT 使用 `idx_user_id` 高效）

**游标分页原理**（以 `ORDER BY create_time DESC` 为例）：

```
LIMIT offset 翻到第 10000 页:
  idx_create_time 扫描方向 → → → → → ... → → → → → → → → (扫描 100000 行)
  [1]...[10]...[1000]...[5000]...[9990]  ← 跳过 9990 行
                                        [9991..10000] ← 返回这 10 行

游标翻到第 10000 页（等价于上一次的最后时间 '2026-05-01'）:
  idx_create_time: ... → '2026-05-01' → ...（最新的在后面）
  WHERE create_time < '2026-05-01'
  B+Tree 直接定位到 '2026-05-01' 位置
  ↓
  [xxxxx] ← 连续读取 10 行，停止
  扫描量固定 = 10 行
```

**改动文件**（7 个文件）：
- `OrderSearchRequest.java` — `pageNum` → `cursor` + `lastId`
- `OrderFilterRequest.java` — 同上
- `Result.java` — 新增 `cursor` + `lastId` 字段及工厂方法
- `OrderMapper.java` — 游标参数签名，新增 `queryUserOrdersWithPage` / `countUserOrders`
- `OrderMapper.xml` — 游标条件 SQL，条件性 JOIN，用户分页 SQL
- `OrderServiceImpl.java` — 游标逻辑，移除 COUNT，修复内存分页
- `OrderController.java` — 适配新参数

**量化结果**：

| 接口 | 场景 | 优化前 | 优化后 | 提升倍数 |
|------|------|--------|--------|---------|
| search | 无关键词 page 1 | 1815 ms | 8 ms | **×227** |
| search | 蓝牙 page 1 | 30 ms | 8 ms | **×4** |
| filter | 无条件 page 1 | 531 ms | 7 ms | **×76** |
| filter | 无条件深翻页（等价 page 10000） | 852 ms | 66 ms | **×13** |
| user | 第 1 页 | ~23 ms（内存分页，查全部） | ~34 ms（SQL 分页，查 10 条） | 内存不再浪费 |

**trade-off**：游标分页不支持页码跳转，前端需改为"加载更多"或"上一页/下一页"交互模式。
