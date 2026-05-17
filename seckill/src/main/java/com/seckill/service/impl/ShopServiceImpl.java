package com.seckill.service.impl;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.bloomfilter.BloomFilter;
import cn.hutool.bloomfilter.bitMap.BitMap;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.dto.Result;
import com.seckill.entity.Shop;
import com.seckill.mapper.ShopMapper;
import com.seckill.service.IShopService;
import com.seckill.utils.CacheClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.seckill.utils.RedisConstants.*;

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
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopMapper shopMapper;

    @Autowired
    private BitMapBloomFilter bloomFilter;

    /**
     * 预加载数据到布隆过滤器
     */
    @PostConstruct
    public void init() {
        List<Object> idObjects = shopMapper.selectObjs(
                new QueryWrapper<Shop>().select("id")
        );

        List<String> allIds = idObjects.stream()
                .map(String::valueOf)
                .toList();

        allIds.forEach(bloomFilter::add);
    }

    @Override
    public boolean save(Shop shop) {
        boolean saved = super.save(shop);
        if (saved) {
            bloomFilter.add(String.valueOf(shop.getId()));
        }
        return saved;
    }

    @Override
    public Result queryByName(String name) {
        List<Shop> shops = query()
                .like("name", name)
                .list();
        if (shops == null || shops.isEmpty()) {
            return Result.fail("店铺不存在");
        }
        if (shops.size() == 1) {
            return Result.ok(shops.get(0));
        }
        return Result.ok(shops);
    }

    @Override
    public Result queryById(Long id) {
        /**
         * baseline
         */
//        Shop shop = shopMapper.selectById(id);

        /**
         * 解决缓存穿透问题 - 缓存空对象
         */
        Shop shop = cacheClient.queryWithPassThrough(
                CACHE_SHOP_KEY,
                id,
                Shop.class,
                (shopId) -> shopMapper.selectById(shopId),
                CACHE_SHOP_TTL,
                TimeUnit.SECONDS
        );

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
//        Shop shop = cacheClient.queryWithLogicalExpire(
//                CACHE_SHOP_KEY,
//                LOCK_SHOP_KEY,
//                id,
//                Shop.class,
//                (shopId) -> shopMapper.selectById(shopId),
//                10L,
//                TimeUnit.SECONDS
//        );

        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺 id 不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY +id);

        return Result.ok();
    }
}
