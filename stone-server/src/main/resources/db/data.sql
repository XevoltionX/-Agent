-- 高翠网 基础数据
USE gaocui;

-- 商家
INSERT IGNORE INTO merchants (id, email, is_vip, vip_start_date, vip_end_date, status) VALUES
(1, 'seller@email.com', 0, NULL, NULL, 1),
(2, 'vip_seller@email.com', 1, '2026-01-01', '2027-01-01', 1);

-- 商品
INSERT IGNORE INTO products (id, merchant_id, title, description, detail, price, status, published_at, created_at) VALUES
(1, 1, '冰种飘花翡翠手镯 58圈口', '天然A货翡翠，冰种质地，飘花灵动，色泽清雅，上手显白', '材质：天然A货翡翠\n种水：冰种\n款式：平安镯\n圈口：58mm\n宽度：14mm\n厚度：8mm\n完美度：无纹无裂', 58000, 'PUBLISHED', '2026-06-15 14:30:00', '2026-06-14 09:20:00'),
(2, 1, '帝王绿翡翠蛋面戒面 18K金镶嵌', '帝王绿色正浓阳匀，起荧光，可做戒指吊坠两用', '材质：天然A货翡翠\n颜色：帝王绿\n尺寸：8.5×7.2×3.8mm\n镶嵌：18K金+南非钻\n完美度：无纹无裂', 128000, 'PUBLISHED', '2026-06-16 10:15:00', '2026-06-15 16:40:00'),
(3, 2, '冰种翡翠平安扣 饱满厚装', '冰种质地，饱满圆润，平安吉祥，送礼自用皆宜', '材质：天然A货翡翠\n种水：冰种\n直径：32mm\n厚度：6.5mm\n完美度：无纹无裂', 18000, 'PUBLISHED', '2026-06-13 08:30:00', '2026-06-12 11:20:00'),
(4, 1, '紫罗兰翡翠观音挂件 手工精雕', '紫罗兰色观音，雕工精细，面容慈祥，保平安', '材质：天然A货翡翠\n颜色：紫罗兰\n尺寸：48×30×8mm\n雕工：手工精雕', 35000, 'DRAFT', NULL, '2026-06-16 15:10:00'),
(5, 1, '玻璃种翡翠叶子 起光起胶', '玻璃种质地，起光起胶，事业有成寓意', '材质：天然A货翡翠\n种水：玻璃种\n尺寸：38×18×4mm\n寓意：事业有成', 68000, 'DELISTED', '2026-06-10 09:00:00', '2026-06-09 13:30:00'),
(6, 2, '冰种翡翠吊坠 葫芦造型 福禄双全', '冰种葫芦吊坠，器型饱满，寓意福禄，送礼自用均可', '材质：天然A货翡翠\n种水：冰种\n尺寸：35×18×6mm\n造型：葫芦\n寓意：福禄双全', 25000, 'PUBLISHED', '2026-06-12 09:45:00', '2026-06-11 17:00:00');

-- 商品图片（用picsum占位图）
INSERT IGNORE INTO product_images (product_id, image_url, sort_order) VALUES
(1, 'https://picsum.photos/seed/jade1/400/400', 0),
(1, 'https://picsum.photos/seed/jade2/400/400', 1),
(1, 'https://picsum.photos/seed/jade3/400/400', 2),
(2, 'https://picsum.photos/seed/jade4/400/400', 0),
(2, 'https://picsum.photos/seed/jade5/400/400', 1),
(3, 'https://picsum.photos/seed/jade6/400/400', 0),
(3, 'https://picsum.photos/seed/jade7/400/400', 1),
(4, 'https://picsum.photos/seed/jade8/400/400', 0),
(5, 'https://picsum.photos/seed/jade9/400/400', 0),
(6, 'https://picsum.photos/seed/jade10/400/400', 0),
(6, 'https://picsum.photos/seed/jade11/400/400', 1);

-- 商品标签
INSERT IGNORE INTO product_tags (product_id, tag_name, sort_order) VALUES
(1, '冰种', 0), (1, '手镯', 1), (1, '飘花', 2), (1, '58圈口', 3),
(2, '帝王绿', 0), (2, '戒面', 1), (2, '蛋面', 2), (2, '镶嵌', 3),
(3, '冰种', 0), (3, '平安扣', 1), (3, '挂件', 2), (3, '送礼', 3),
(4, '紫罗兰', 0), (4, '观音', 1), (4, '挂件', 2),
(5, '玻璃种', 0), (5, '叶子', 1), (5, '挂件', 2), (5, '高货', 3),
(6, '冰种', 0), (6, '吊坠', 1), (6, '葫芦', 2), (6, '送礼', 3);

-- 客资
INSERT IGNORE INTO leads (merchant_id, product_id, buyer_email, buyer_message, status, created_at) VALUES
(1, 1, 'buyer123@qq.com', '你好，请问这个手镯有纹裂吗？能否拍个视频看看？预算5万左右，圈口合适的话可以马上定', 'PENDING', '2026-06-17 10:30:00'),
(1, 2, 'jade_lover@163.com', '帝王绿戒面，请问颜色有没有偏蓝调？实物和图片色差大吗？', 'CONTACTED', '2026-06-16 14:20:00'),
(1, 3, 'gift_buyer@126.com', '想买来送妈妈的，2万以内可以的，请问有证书吗？', 'PENDING', '2026-06-17 09:15:00'),
(1, 6, 'emily_wang@outlook.com', '葫芦吊坠很好看，请问可以优惠一点吗？预算2万以内', 'PENDING', '2026-06-16 16:45:00');

-- 系统通知
INSERT IGNORE INTO notifications (merchant_id, type, title, content, is_read, created_at) VALUES
(1, 'NEW_LEAD', '您有一条新的客户留资——冰种飘花翡翠手镯', '买家buyer123@qq.com对您的商品"冰种飘花翡翠手镯 58圈口"提交了咨询', 0, '2026-06-17 10:30:00'),
(1, 'NEW_LEAD', '您有一条新的客户留资——冰种翡翠平安扣', '买家gift_buyer@126.com对您的商品"冰种翡翠平安扣 饱满厚装"提交了咨询', 0, '2026-06-17 09:15:00'),
(1, 'VIP_EXPIRY', 'VIP会员即将到期提醒', '您的VIP会员将于30天后到期，请及时续费以继续享受VIP权益', 0, '2026-06-16 08:00:00');
