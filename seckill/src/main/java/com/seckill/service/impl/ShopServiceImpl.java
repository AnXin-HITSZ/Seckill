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
        Shop shop = cacheClient.queryWithPassThrough(
                CACHE_SHOP_KEY,
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
