package com.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.dto.Result;
import com.seckill.entity.Shop;
import com.seckill.mapper.ShopMapper;
import com.seckill.service.IShopService;
import com.seckill.utils.CacheClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.seckill.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.seckill.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * ClassName: ShopServiceImpl
 * Package: com.seckill.service.impl
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 20:47
 * @Version 1.0
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopMapper shopMapper;

    @Override
    public Result queryById(Long id) {
        /**
         * baseline
         */
//        Shop shop = shopMapper.selectById(id);

        /**
         * 解决缓存穿透问题 - 缓存空对象
         */
//        Shop shop = cacheClient.queryWithPassThrough(
//                CACHE_SHOP_KEY,
//                id,
//                Shop.class,
//                (shopId) -> shopMapper.selectById(shopId),
//                CACHE_SHOP_TTL,
//                TimeUnit.SECONDS
//        );

        /**
         * 解决缓存击穿问题 - 互斥锁
         */
//        Shop shop = cacheClient.queryWithMutex(
//                CACHE_SHOP_KEY,
//                LOCK_SHOP_KEY,
//                id,
//                Shop.class,
//                (shopId) -> shopMapper.selectById(shopId),
//                10L,
//                TimeUnit.SECONDS
//        );

        /**
         * 解决缓存击穿问题 - 逻辑过期
         */
//        cacheClient.saveToRedis(
//                CACHE_SHOP_KEY,
//                id,
//                10L,
//                Shop.class,
//                (shopId) -> shopMapper.selectById(shopId)
//        );
        Shop shop = cacheClient.queryWithLogicalExpire(
                CACHE_SHOP_KEY,
                LOCK_SHOP_KEY,
                id,
                Shop.class,
                (shopId) -> shopMapper.selectById(shopId),
                10L,
                TimeUnit.SECONDS
        );

        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }
}
