package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.dto.CategoryStatsVO;
import com.seckill.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    // ---- 游标分页查询 ----

    List<Order> searchOrders(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime,
                             @Param("minAmount") Long minAmount,
                             @Param("productName") String productName,
                             @Param("cursor") LocalDateTime cursor,
                             @Param("lastId") Long lastId,
                             @Param("pageSize") int pageSize);

    List<Order> filterOrders(@Param("userId") Long userId,
                             @Param("statusList") List<Integer> statusList,
                             @Param("categoryIds") List<Long> categoryIds,
                             @Param("cursor") LocalDateTime cursor,
                             @Param("lastId") Long lastId,
                             @Param("pageSize") int pageSize);

    // ---- 传统分页（用户订单量小，保留页码跳转） ----

    List<Order> queryUserOrdersWithPage(@Param("userId") Long userId,
                                        @Param("status") Integer status,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    long countUserOrders(@Param("userId") Long userId,
                         @Param("status") Integer status);

    // ---- 统计查询 ----

    List<CategoryStatsVO> selectCategoryStats();
}
