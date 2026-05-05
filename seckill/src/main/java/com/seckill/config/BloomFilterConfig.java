package com.seckill.config;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: BloomFilterConfig
 * Package: com.seckill.config
 * Description:
 *
 * @Author AnXin
 * @Create 2026/5/5 14:16
 * @Version 1.0
 */
@Configuration
public class BloomFilterConfig {

    @Bean
    public BitMapBloomFilter bitMapBloomFilter() {
        // 参数表示内存大小(MB)，5MB可容纳数百万个元素
        return new BitMapBloomFilter(5);
    }
}
