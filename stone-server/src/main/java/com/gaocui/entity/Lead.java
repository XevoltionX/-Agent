package com.gaocui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("leads")
public class Lead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long productId;
    private String buyerEmail;
    private String buyerMessage;
    private String status;          // PENDING / CONTACTED
    private LocalDateTime createdAt;
}
