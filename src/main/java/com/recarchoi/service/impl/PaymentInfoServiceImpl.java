package com.recarchoi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recarchoi.entity.PaymentInfo;
import com.recarchoi.mapper.PaymentInfoMapper;
import com.recarchoi.service.PaymentInfoService;
import org.springframework.stereotype.Service;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

}
