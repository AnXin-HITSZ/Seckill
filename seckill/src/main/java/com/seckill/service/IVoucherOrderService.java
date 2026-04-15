package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.dto.Result;
import com.seckill.entity.VoucherOrder;

/**
 * ClassName: IVoucherOrderService
 * Package: com.seckill.service
 * Description:
 *
 * @Author AnXin
 * @Create 2026/4/15 16:35
 * @Version 1.0
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);
}
