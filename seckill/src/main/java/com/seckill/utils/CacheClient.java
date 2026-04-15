package com.seckill.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.seckill.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    private void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
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
    public <R, ID> R queryWithMutex(
            String keyPrefix,
            String lockPrefix,
            ID id,
            Class<R> rType,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit
    ) {
        String key = keyPrefix + id;
        // 1. 从 Redis 查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 2.1 存在，直接返回
            return JSONUtil.toBean(json, rType);
        }
        if (json != null) {
            return null;
        }
        // 2.2 不存在，实现缓存重建
        String lockKey = lockPrefix + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, lockPrefix, id, rType, dbFallback, time, unit);
            }
            r = dbFallback.apply(id);
            Thread.sleep(200);
            if (r == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unLock(lockKey);
        }
        // 3. 返回
        return r;
    }

    /**
     * 缓存预热
     */
    public <ID, R> void saveToRedis(
            String keyPrefix,
            ID id,
            Long expireSeconds,
            Class<R> rType,
            Function<ID, R> dbFallback
    ) {
        String key = keyPrefix + id;
        // 1. 查询店铺数据
        R r = dbFallback.apply(id);
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 2. 封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(r);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3. 写入 Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 解决缓存击穿问题 - 逻辑过期
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix,
            String lockPrefix,
            ID id,
            Class<R> rType,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit
    ) {
        String key = keyPrefix + id;
        // 1. 从 Redis 查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isBlank(json)) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), rType);
        LocalDateTime expireTime = redisData.getExpireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }
        String lockKey = lockPrefix + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R searchR = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, searchR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unLock(lockKey);
                }
            });
        }
        return r;
    }

    public Boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", CACHE_LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    public void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
