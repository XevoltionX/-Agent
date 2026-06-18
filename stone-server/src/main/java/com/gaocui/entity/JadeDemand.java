package com.gaocui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent分析翡翠需求的结果（内部用→喂给ES搜索，不对用户展示）
 */
@Data
@TableName("jade_demands")
public class JadeDemand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;           // 对话session
    private String userMessage;         // 用户原始输入
    private String demandTitle;         // 15字需求标题
    private String demandDescription;   // 50字需求描述
    private String demandDetail;        // 300字详细描述
    private String tags;                // JSON数组: 10个标签
    private String specs;               // JSON对象: 14个参数
    private String esKeyword;           // 组装后给ES搜索的keyword
    private String esTags;              // 组装后给ES搜索的tags
    private Integer matchCount;         // 匹配到的商品数
    private LocalDateTime createdAt;
}
