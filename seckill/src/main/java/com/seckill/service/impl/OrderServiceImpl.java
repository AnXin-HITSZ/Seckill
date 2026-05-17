package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.dto.CategoryStatsVO;
import com.seckill.dto.Result;
import com.seckill.entity.Order;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.IOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public Result queryUserOrders(Long userId, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;

        List<Order> list = orderMapper.queryUserOrdersWithPage(userId, status, offset, pageSize);
        long total = orderMapper.countUserOrders(userId, status);

        return Result.ok(list, total);
    }

    @Override
    public Result searchOrders(String startTime, String endTime, Long minAmount,
                               String productName, String cursor, Long lastId, int pageSize) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = startTime != null && !startTime.isEmpty()
                ? LocalDateTime.parse(startTime + " 00:00:00", formatter) : null;
        LocalDateTime end = endTime != null && !endTime.isEmpty()
                ? LocalDateTime.parse(endTime + " 23:59:59", formatter) : null;
        LocalDateTime cursorTime = cursor != null ? LocalDateTime.parse(cursor) : null;

        List<Order> list = orderMapper.searchOrders(start, end, minAmount, productName, cursorTime, lastId, pageSize);

        // 为下一页准备游标
        String nextCursor = null;
        Long nextLastId = null;
        if (!list.isEmpty() && list.size() >= pageSize) {
            Order last = list.get(list.size() - 1);
            nextCursor = last.getCreateTime().toString();
            nextLastId = last.getId();
        }

        return Result.ok(list, nextCursor, nextLastId);
    }

    @Override
    public Result getCategoryStats() {
        List<CategoryStatsVO> stats = orderMapper.selectCategoryStats();
        return Result.ok(stats);
    }

    @Override
    public Result filterOrders(Long userId, List<Integer> statusList,
                               List<Long> categoryIds, String cursor, Long lastId, int pageSize) {
        LocalDateTime cursorTime = cursor != null ? LocalDateTime.parse(cursor) : null;

        List<Order> list = orderMapper.filterOrders(userId, statusList, categoryIds, cursorTime, lastId, pageSize);

        // 为下一页准备游标
        String nextCursor = null;
        Long nextLastId = null;
        if (!list.isEmpty() && list.size() >= pageSize) {
            Order last = list.get(list.size() - 1);
            nextCursor = last.getCreateTime().toString();
            nextLastId = last.getId();
        }

        return Result.ok(list, nextCursor, nextLastId);
    }
}
