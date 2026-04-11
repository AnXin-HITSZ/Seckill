package com.seckill.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.seckill.utils.RedisConstants.*;

/**
 * ClassName: CacheClient
 * Package: com.utils
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 20:53
 * @Version 1.0
 */
@Slf4j
@Component
public class CacheClient {

    private StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 解决缓存穿透问题 - 缓存空对象
     */
    public <R, ID> R queryWithPassThrough(
            String keyPrefix,
            ID id,
            Class<R> rType,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit
    ) {
        String key = keyPrefix + id.toString();
        // 1. 从 Redis 查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotEmpty(json)) {
            // 2.1 存在，返回
            log.debug("该商铺存在（未查询数据库）：{}", json);
            return JSONUtil.toBean(json, rType);
        }
        // 2.2 不存在
        if (json != null) {
            log.debug("该商铺不存在（未查询数据库）");
            return null;
        }
        R r = dbFallback.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            log.debug("该商铺不存在（已查询数据库 - 缓存空对象）");
            return null;
        }
        log.debug("该商铺存在（已查询数据库）");
        set(key, r, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return r;
    }

    /**
     * 解决缓存击穿问题 - 互斥锁
     */
    public <R, ID> R queryWithMutex(ID id, Class<R> rType) {
        String key = CACHE_SHOP_KEY + id;
        // 1. 从 Redis 查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 2.1 存在，直接返回
            return JSONUtil.toBean(shopJson, rType);
        }
        // 2.2 不存在，实现缓存重建

        // 3. 返回

    }

    public Boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", CACHE_LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    public void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
