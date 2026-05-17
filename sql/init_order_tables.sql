-- ============================================================
-- Seckill 项目 - 完整数据库初始化脚本
-- ============================================================

-- --------------------- 秒杀相关表 ---------------------

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    phone VARCHAR(11) NOT NULL COMMENT '手机号码',
    password VARCHAR(128) DEFAULT '' COMMENT '密码',
    nick_name VARCHAR(32) DEFAULT '' COMMENT '昵称',
    icon VARCHAR(255) DEFAULT '' COMMENT '头像',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商铺表
CREATE TABLE IF NOT EXISTS tb_shop (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(128) NOT NULL COMMENT '商铺名称',
    type_id BIGINT DEFAULT 0 COMMENT '商铺类型ID',
    images VARCHAR(1024) DEFAULT '' COMMENT '商铺图片',
    area VARCHAR(128) DEFAULT '' COMMENT '商圈',
    address VARCHAR(255) DEFAULT '' COMMENT '地址',
    x DOUBLE DEFAULT 0 COMMENT '经度',
    y DOUBLE DEFAULT 0 COMMENT '纬度',
    avg_price BIGINT DEFAULT 0 COMMENT '均价(分)',
    sold INT DEFAULT 0 COMMENT '销量',
    comments INT DEFAULT 0 COMMENT '评论数量',
    score INT DEFAULT 0 COMMENT '评分(1~50, 乘10保存)',
    open_hours VARCHAR(64) DEFAULT '' COMMENT '营业时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type_id (type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS tb_voucher (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    shop_id BIGINT DEFAULT 0 COMMENT '商铺ID',
    title VARCHAR(255) DEFAULT '' COMMENT '标题',
    sub_title VARCHAR(255) DEFAULT '' COMMENT '副标题',
    rules VARCHAR(1024) DEFAULT '' COMMENT '使用规则',
    pay_value BIGINT DEFAULT 0 COMMENT '支付金额(分)',
    actual_value BIGINT DEFAULT 0 COMMENT '抵扣金额(分)',
    type TINYINT DEFAULT 0 COMMENT '优惠券类型: 0-普通券 1-秒杀券',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-上架 2-下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 秒杀券表（与 tb_voucher 一对一关联）
CREATE TABLE IF NOT EXISTS tb_seckill_voucher (
    voucher_id BIGINT PRIMARY KEY COMMENT '关联的优惠券ID',
    stock INT DEFAULT 0 COMMENT '库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    begin_time DATETIME NOT NULL COMMENT '生效时间',
    end_time DATETIME NOT NULL COMMENT '失效时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀券表';

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS tb_voucher_order (
    id BIGINT PRIMARY KEY COMMENT '主键(使用分布式ID)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    voucher_id BIGINT NOT NULL COMMENT '优惠券ID',
    pay_type TINYINT DEFAULT 1 COMMENT '支付方式: 1-余额 2-支付宝 3-微信',
    status TINYINT DEFAULT 1 COMMENT '订单状态: 1-未支付 2-已支付 3-已核销 4-已取消 5-退款中 6-已退款',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    use_time DATETIME DEFAULT NULL COMMENT '核销时间',
    refund_time DATETIME DEFAULT NULL COMMENT '退款时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_voucher_id (voucher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- --------------------- 订单查询相关表 ---------------------

-- 订单主表
CREATE TABLE IF NOT EXISTS tb_order (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 0 COMMENT '订单状态: 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已退款',
    total_amount BIGINT DEFAULT 0 COMMENT '订单总金额(分)',
    category_id BIGINT DEFAULT 0 COMMENT '品类ID',
    category_name VARCHAR(100) DEFAULT '' COMMENT '品类名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单项表（含商品名称，用于关键词搜索）
CREATE TABLE IF NOT EXISTS tb_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_name VARCHAR(255) DEFAULT '' COMMENT '商品名称',
    quantity INT DEFAULT 1 COMMENT '数量',
    price BIGINT DEFAULT 0 COMMENT '单价(分)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';
