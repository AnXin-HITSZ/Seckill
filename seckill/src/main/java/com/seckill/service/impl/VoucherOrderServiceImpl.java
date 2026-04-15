package com.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.dto.Result;
import com.seckill.entity.VoucherOrder;
import com.seckill.service.IVoucherOrderService;
import org.springframework.stereotype.Service;

/**
 * ClassName: VoucherOrderServiceImpl
 * Package: com.seckill.service.impl
 * Description:
 *
 * @Author AnXin
 * @Create 2026/4/15 16:45
 * @Version 1.0
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrder> implements IVoucherOrderService {
    @Override
    public Result seckillVoucher(Long voucherId) {
        return null;
    }
}
