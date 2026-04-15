package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.Voucher;

/**
 * ClassName: IVoucherService
 * Package: com.seckill.service
 * Description:
 *
 * @Author AnXin
 * @Create 2026/4/15 16:04
 * @Version 1.0
 */
public interface IVoucherService extends IService<Voucher> {
    void addSeckillVoucher(Voucher voucher);
}
