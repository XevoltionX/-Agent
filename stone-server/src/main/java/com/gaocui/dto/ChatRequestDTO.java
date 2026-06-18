package com.gaocui.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequestDTO {
    private String message;          // 用户输入
    private String sessionId;        // 会话ID（预留Agent多轮对话）
    private List<Map<String, Object>> history;  // 历史消息（预留Agent上下文）
}
