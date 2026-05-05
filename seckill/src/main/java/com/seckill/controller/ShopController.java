package com.seckill.controller;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.seckill.dto.Result;
import com.seckill.entity.Shop;
import com.seckill.service.IShopService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: ShopController
 * Package: com.seckill.controller
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 20:44
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private BitMapBloomFilter bloomFilter;

    @Resource
    private IShopService shopService;

    /**
     * 新增商铺信息
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    /**
     * 查询商铺信息
     */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /**
     * 更新商铺信息
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // 写入数据库
        return shopService.update(shop);
    }
}
