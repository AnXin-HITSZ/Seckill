package com.seckill.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataGenerator {

    private static final Logger log = LoggerFactory.getLogger(DataGenerator.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final int BATCH_SIZE = 50000;

    private static final List<String> CATEGORIES = List.of(
            "电子产品", "家居用品", "服装鞋帽", "食品饮料", "图书文具",
            "运动户外", "美妆个护", "母婴用品", "宠物用品", "汽车配件",
            "数码配件", "家用电器", "乐器", "办公设备", "玩具",
            "珠宝首饰", "医疗保健", "箱包", "厨具", "家纺"
    );

    private static final Map<String, List<String>> PRODUCTS = new LinkedHashMap<>();

    static {
        PRODUCTS.put("电子产品", List.of("蓝牙耳机", "蓝牙音箱", "无线鼠标", "机械键盘", "移动电源",
                "智能手表", "平板电脑", "蓝牙键盘", "USB集线器", "手机壳"));
        PRODUCTS.put("家居用品", List.of("智能台灯", "香薰机", "加湿器", "空气净化器", "吸尘器",
                "智能插座", "垃圾桶", "收纳盒", "衣架", "地毯"));
        PRODUCTS.put("服装鞋帽", List.of("运动鞋", "羽绒服", "T恤", "牛仔裤", "帽子",
                "围巾", "手套", "休闲裤", "卫衣", "夹克"));
        PRODUCTS.put("食品饮料", List.of("咖啡豆", "绿茶", "坚果礼盒", "巧克力", "蜂蜜",
                "饼干", "牛肉干", "酸奶机", "矿泉水", "红酒"));
        PRODUCTS.put("图书文具", List.of("笔记本", "钢笔", "书架", "台历", "便签纸",
                "文件夹", "计算器", "订书机", "胶带", "马克笔"));
        PRODUCTS.put("运动户外", List.of("瑜伽垫", "跑步机", "哑铃", "跳绳", "帐篷",
                "登山杖", "水壶", "护膝", "泳镜", "自行车"));
        PRODUCTS.put("美妆个护", List.of("面霜", "洗面奶", "防晒霜", "口红", "香水",
                "洗发水", "护手霜", "面膜", "眼影", "化妆刷"));
        PRODUCTS.put("母婴用品", List.of("奶瓶", "纸尿裤", "婴儿车", "玩具", "湿巾",
                "婴儿床", "学步车", "安全座椅", "奶粉", "吸奶器"));
        PRODUCTS.put("宠物用品", List.of("猫粮", "狗粮", "猫砂", "宠物窝", "牵引绳",
                "玩具球", "食盆", "梳子", "宠物背包", "磨牙棒"));
        PRODUCTS.put("数码配件", List.of("充电宝", "数据线", "手机支架", "蓝牙耳机", "充电器",
                "手机贴膜", "摄像头", "读卡器", "耳机", "自拍杆"));
    }

    @PostConstruct
    public void init() {
        log.info("DataGenerator initialized. Call POST /api/orders/generate to generate test data.");
    }

    public void generateOrders(int totalCount) {
        log.info("开始生成 {} 条订单测试数据...", totalCount);
        long startTime = System.currentTimeMillis();

        // truncate existing data
        jdbcTemplate.execute("TRUNCATE TABLE tb_order_item");
        jdbcTemplate.execute("TRUNCATE TABLE tb_order");
        log.info("已清空旧数据");

        int batches = (totalCount + BATCH_SIZE - 1) / BATCH_SIZE;
        int generatedCount = 0;

        for (int batch = 0; batch < batches; batch++) {
            int currentBatchSize = Math.min(BATCH_SIZE, totalCount - generatedCount);

            List<Object[]> orderBatch = new ArrayList<>(currentBatchSize);
            List<Object[]> itemBatch = new ArrayList<>();

            for (int i = 0; i < currentBatchSize; i++) {
                long userId = ThreadLocalRandom.current().nextLong(1, 10001);
                int status = weightedRandomStatus();
                long amount = ThreadLocalRandom.current().nextLong(1000, 100001);
                int categoryIdx = ThreadLocalRandom.current().nextInt(CATEGORIES.size());
                long categoryId = categoryIdx + 1;
                String categoryName = CATEGORIES.get(categoryIdx);

                LocalDateTime createTime = randomDateTime();
                LocalDateTime updateTime = createTime.plusDays(ThreadLocalRandom.current().nextInt(1, 30));

                long orderId = generatedCount + i + 1;
                orderBatch.add(new Object[]{
                        orderId, userId, status, amount, categoryId, categoryName,
                        createTime, updateTime
                });

                // generate 1-3 items per order
                int itemCount = ThreadLocalRandom.current().nextInt(1, 4);
                List<String> categoryProducts = PRODUCTS.getOrDefault(categoryName,
                        List.of("蓝牙耳机", "普通商品", "日用品"));
                for (int j = 0; j < itemCount; j++) {
                    String productName = categoryProducts.get(
                            ThreadLocalRandom.current().nextInt(categoryProducts.size()));
                    int quantity = ThreadLocalRandom.current().nextInt(1, 6);
                    long price = ThreadLocalRandom.current().nextLong(100, 50001);
                    itemBatch.add(new Object[]{
                            orderId, productName, quantity, price, createTime
                    });
                }
            }

            // batch insert within a single transaction for speed
            List<Object[]> finalOrderBatch = orderBatch;
            List<Object[]> finalItemBatch = itemBatch;
            transactionTemplate.execute(status -> {
                jdbcTemplate.batchUpdate(
                        "INSERT INTO tb_order (id, user_id, status, total_amount, category_id, category_name, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        finalOrderBatch
                );
                jdbcTemplate.batchUpdate(
                        "INSERT INTO tb_order_item (order_id, product_name, quantity, price, create_time) VALUES (?, ?, ?, ?, ?)",
                        finalItemBatch
                );
                return null;
            });

            generatedCount += currentBatchSize;
            log.info("数据生成进度: {}/{} (订单 {} 条, 订单项 {} 条)",
                    generatedCount, totalCount, generatedCount, itemBatch.size());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("测试数据生成完成! 共 {} 条订单, 耗时: {} 秒", generatedCount, elapsed / 1000.0);
    }

    private int weightedRandomStatus() {
        // 0:pending 1:paid 2:shipped 3:completed 4:cancelled 5:refunded
        double r = ThreadLocalRandom.current().nextDouble();
        if (r < 0.05) return 0;      // 5% 待支付
        if (r < 0.30) return 1;      // 25% 已支付
        if (r < 0.50) return 2;      // 20% 已发货
        if (r < 0.85) return 3;      // 35% 已完成
        if (r < 0.95) return 4;      // 10% 已取消
        return 5;                     // 5% 已退款
    }

    private LocalDateTime randomDateTime() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 30, 23, 59);
        long seconds = ThreadLocalRandom.current().nextLong(
                java.time.Duration.between(start, end).getSeconds());
        return start.plusSeconds(seconds);
    }
}
