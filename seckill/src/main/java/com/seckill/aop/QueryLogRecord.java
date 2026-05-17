package com.seckill.aop;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class QueryLogRecord {
    private String methodName;
    private String args;
    private long durationMs;
    private long thresholdMs;
    private LocalDateTime timestamp;

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public boolean isSlow() {
        return durationMs >= thresholdMs;
    }
}
