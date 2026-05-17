package com.seckill.controller;

import com.seckill.aop.QueryLogCollector;
import com.seckill.aop.QueryLogRecord;
import com.seckill.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Resource
    private QueryLogCollector queryLogCollector;

    @GetMapping("/queries")
    public Result getRecentQueries() {
        List<QueryLogRecord> records = queryLogCollector.getRecent();
        return Result.ok(records);
    }

    @DeleteMapping("/queries")
    public Result clearQueries() {
        queryLogCollector.clear();
        return Result.ok();
    }
}
