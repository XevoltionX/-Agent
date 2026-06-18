package com.gaocui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("verification_codes")
public class VerificationCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String code;
    private String type;            // LOGIN / CHANGE_EMAIL
    private LocalDateTime expiresAt;
    private Integer isUsed;         // 0=未使用, 1=已使用
    private LocalDateTime createdAt;
}
