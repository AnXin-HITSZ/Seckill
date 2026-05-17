package com.seckill.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderFilterRequest {
    private Long userId;
    private List<Integer> statusList;
    private List<Long> categoryIds;
    /** 上一页最后一条的 create_time (ISO-8601)，null 表示第一页 */
    private String cursor;
    /** 上一页最后一条的 id，时间戳重复时作 tiebreaker */
    private Long lastId;
    private int pageSize = 10;
}
