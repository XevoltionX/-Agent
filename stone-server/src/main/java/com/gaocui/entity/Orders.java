package com.gaocui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;       // 订单号
    private Long merchantId;      // 商家ID
    private String planType;      // 12month / 6month
    private BigDecimal total;     // 金额
    private String status;        // 待支付 / 已支付 / 已退款
    private String payNo;         // 支付宝交易号
    private String payTime;       // 付款时间
    private LocalDateTime createdAt;
}
