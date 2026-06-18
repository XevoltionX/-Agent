package com.gaocui.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PublishProductDTO {
    private String title;
    private String description;
    private String detail;
    private BigDecimal price;
    private String status;       // PUBLISHED / DRAFT，默认PUBLISHED
    private List<String> tags;
    private List<String> images;  // base64或URL列表
}
