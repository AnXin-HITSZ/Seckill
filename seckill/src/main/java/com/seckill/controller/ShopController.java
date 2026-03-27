package com.seckill.controller;

import com.seckill.dto.Result;
import com.seckill.service.IShopService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Resource
    private IShopService shopService;

    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }
}
