package com.seckill.controller;

import com.seckill.config.DataGenerator;
import com.seckill.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class DataGenerateController {

    @Resource
    private DataGenerator dataGenerator;

    @PostMapping("/generate")
    public Result generateData(@RequestParam(defaultValue = "1000000") int count) {
        new Thread(() -> dataGenerator.generateOrders(count)).start();
        return Result.ok("数据生成任务已启动，请查看日志监控进度。生成量: " + count + " 条订单");
    }
}
