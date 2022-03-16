package com.recarchoi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recarchoi.entity.OrderInfo;
import com.recarchoi.mapper.OrderInfoMapper;
import com.recarchoi.service.OrderInfoService;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

}
