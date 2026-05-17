package com.seckill.controller;

import com.seckill.dto.OrderFilterRequest;
import com.seckill.dto.OrderSearchRequest;
import com.seckill.dto.Result;
import com.seckill.service.IOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Resource
    private IOrderService orderService;

    @PostMapping("/user")
    public Result queryUserOrders(@RequestParam Long userId,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return orderService.queryUserOrders(userId, status, pageNum, pageSize);
    }

    @PostMapping("/search")
    public Result searchOrders(@RequestBody OrderSearchRequest request) {
        return orderService.searchOrders(
                request.getStartTime(),
                request.getEndTime(),
                request.getMinAmount(),
                request.getProductName(),
                request.getCursor(),
                request.getLastId(),
                request.getPageSize()
        );
    }

    @GetMapping("/category-stats")
    public Result getCategoryStats() {
        return orderService.getCategoryStats();
    }

    @PostMapping("/filter")
    public Result filterOrders(@RequestBody OrderFilterRequest request) {
        return orderService.filterOrders(
                request.getUserId(),
                request.getStatusList(),
                request.getCategoryIds(),
                request.getCursor(),
                request.getLastId(),
                request.getPageSize()
        );
    }
}
