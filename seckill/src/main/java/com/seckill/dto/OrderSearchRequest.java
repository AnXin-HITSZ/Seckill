package com.seckill.dto;

import lombok.Data;

@Data
public class OrderSearchRequest {
    private String startTime;
    private String endTime;
    private Long minAmount;
    private String productName;
    /** 上一页最后一条的 create_time (ISO-8601)，null 表示第一页 */
    private String cursor;
    /** 上一页最后一条的 id，时间戳重复时作 tiebreaker */
    private Long lastId;
    private int pageSize = 10;
}
