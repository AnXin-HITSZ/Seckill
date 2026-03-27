package com.seckill.service;

import com.seckill.dto.Result;

/**
 * ClassName: IShopService
 * Package: com.seckill.service
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 20:47
 * @Version 1.0
 */
public interface IShopService {
    Result queryById(Long id);
}
