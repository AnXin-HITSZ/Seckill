package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.SeckillVoucher;
import com.seckill.mapper.SeckillVoucherMapper;
import com.seckill.mapper.VoucherMapper;
import com.seckill.service.ISeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * ClassName: SeckillVoucherServiceImpl
 * Package: com.seckill.service.impl
 * Description:
 *
 * @Author AnXin
 * @Create 2026/4/15 16:18
 * @Version 1.0
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {
}
