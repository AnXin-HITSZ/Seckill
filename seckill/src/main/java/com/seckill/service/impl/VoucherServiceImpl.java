package com.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.SeckillVoucher;
import com.seckill.entity.Voucher;
import com.seckill.mapper.VoucherMapper;
import com.seckill.service.ISeckillVoucherService;
import com.seckill.service.IVoucherService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.seckill.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * ClassName: VoucherServiceImpl
 * Package: com.seckill.service.impl
 * Description:
 *
 * @Author AnXin
 * @Create 2026/4/15 16:07
 * @Version 1.0
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        save(voucher);
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
