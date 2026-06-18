-- 高翠网 数据库建表脚本
CREATE DATABASE IF NOT EXISTS gaocui DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gaocui;

-- 商家表
CREATE TABLE IF NOT EXISTS merchants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_vip TINYINT DEFAULT 0 COMMENT '1=VIP 0=免费',
    vip_start_date DATE NULL,
    vip_end_date DATE NULL,
    notify_email TINYINT DEFAULT 1 COMMENT '1=接收邮件通知',
    status TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'LOGIN/CHANGE_EMAIL',
    expires_at DATETIME NOT NULL,
    is_used TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email_type (email, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    detail TEXT,
    price DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/DELISTED',
    published_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_merchant_status (merchant_id, status),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url MEDIUMTEXT NOT NULL COMMENT '前端展示用(base64/picsum)',
    storage_url VARCHAR(500) NULL COMMENT '千问oss://临时URL(48h有效)',
    uploaded_at DATETIME NULL COMMENT '上传千问时间',
    sort_order INT DEFAULT 0,
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 商品标签表
CREATE TABLE IF NOT EXISTS product_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品标签表';

-- 客资表
CREATE TABLE IF NOT EXISTS leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    buyer_email VARCHAR(255) NOT NULL,
    buyer_message TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/CONTACTED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant (merchant_id),
    INDEX idx_status (merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客资表';

-- 订单表（支付宝沙箱支付）
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    merchant_id BIGINT NOT NULL,
    plan_type VARCHAR(20) NOT NULL COMMENT '12month/6month',
    total DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT '待支付' COMMENT '待支付/已支付/已退款',
    pay_no VARCHAR(64) NULL,
    pay_time VARCHAR(30) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单';

-- Agent翡翠需求分析表（内部用→喂ES搜索，不对用户展示）
CREATE TABLE IF NOT EXISTS jade_demands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    user_message TEXT COMMENT '用户原始输入',
    demand_title VARCHAR(50) COMMENT '15字需求标题',
    demand_description VARCHAR(200) COMMENT '50字需求描述',
    demand_detail TEXT COMMENT '300字详细描述',
    tags TEXT COMMENT 'JSON数组-10个标签',
    specs TEXT COMMENT 'JSON对象-14个参数',
    es_keyword VARCHAR(200) COMMENT '组装后给ES搜索的keyword',
    es_tags VARCHAR(200) COMMENT '组装后给ES搜索的tags',
    match_count INT DEFAULT 0 COMMENT '匹配商品数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent翡翠需求分析';

-- 对话历史表
CREATE TABLE IF NOT EXISTS chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(10) NOT NULL COMMENT 'user/ai',
    content TEXT,
    cards MEDIUMTEXT COMMENT 'JSON数组-商品卡片',
    merchant_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史';

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'NEW_LEAD/VIP_EXPIRY',
    title VARCHAR(255) NOT NULL,
    content TEXT,
    is_read TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant_read (merchant_id, is_read, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
