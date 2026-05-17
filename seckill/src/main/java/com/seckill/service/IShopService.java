package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.dto.Result;
import com.seckill.entity.Shop;

import java.util.List;

/**
 * ClassName: IShopService
 * Package: com.seckill.service
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 20:47
 * @Version 1.0
 */
public interface IShopService extends IService<Shop> {
    Result queryById(Long id);

    Result queryByName(String name);

    Result update(Shop shop);
}
