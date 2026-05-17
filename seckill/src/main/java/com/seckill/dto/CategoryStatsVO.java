package com.seckill.dto;

import lombok.Data;

@Data
public class CategoryStatsVO {
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Long orderCount;
    private Long totalAmount;
}
