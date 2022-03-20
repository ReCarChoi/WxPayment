package com.recarchoi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recarchoi.entity.OrderInfo;

public interface OrderInfoService extends IService<OrderInfo> {
    OrderInfo createOrderByProductId(Long productId);
}
