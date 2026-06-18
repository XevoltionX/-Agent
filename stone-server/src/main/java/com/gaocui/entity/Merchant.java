package com.gaocui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("merchants")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private Integer isVip;          // 1=VIP, 0=免费
    private LocalDate vipStartDate;
    private LocalDate vipEndDate;
    private Integer notifyEmail;    // 1=接收邮件通知
    private Integer status;         // 1=正常, 0=禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
