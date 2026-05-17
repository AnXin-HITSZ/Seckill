package com.seckill.aop;

import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger("SLOW_QUERY");

    @Resource
    private QueryLogCollector queryLogCollector;

    @Value("${performance.slow-threshold-ms:1000}")
    private long slowThresholdMs;

    @Pointcut("execution(* com.seckill.controller.OrderController.*(..)) || execution(* com.seckill.controller.DataGenerateController.*(..))")
    public void orderControllerMethods() {}

    @Around("orderControllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        String argStr = args.length > 0 ? truncateArgs(args) : "[]";

        if (duration >= slowThresholdMs) {
            log.warn("【慢查询告警】方法={}, 参数={}, 耗时={}ms, 阈值={}ms",
                    methodName, argStr, duration, slowThresholdMs);
        } else {
            log.info("方法={}, 参数={}, 耗时={}ms", methodName, argStr, duration);
        }

        queryLogCollector.record(new QueryLogRecord(methodName, argStr, duration, slowThresholdMs, LocalDateTime.now()));

        return result;
    }

    private String truncateArgs(Object[] args) {
        String raw = Arrays.toString(args);
        return raw.length() > 300 ? raw.substring(0, 300) + "..." : raw;
    }
}
