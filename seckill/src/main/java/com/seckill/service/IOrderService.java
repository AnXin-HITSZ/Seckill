package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.dto.CategoryStatsVO;
import com.seckill.dto.Result;
import com.seckill.entity.Order;

import java.util.List;

public interface IOrderService extends IService<Order> {

    Result queryUserOrders(Long userId, Integer status, int pageNum, int pageSize);

    Result searchOrders(String startTime, String endTime, Long minAmount,
                        String productName, String cursor, Long lastId, int pageSize);

    Result getCategoryStats();

    Result filterOrders(Long userId, List<Integer> statusList,
                        List<Long> categoryIds, String cursor, Long lastId, int pageSize);
}
