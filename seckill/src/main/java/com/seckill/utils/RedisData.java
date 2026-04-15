package com.seckill.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: RedisData
 * Package: com.seckill.utils
 * Description:
 *
 * @Author AnXin
 * @Create 2026/4/15 14:46
 * @Version 1.0
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
