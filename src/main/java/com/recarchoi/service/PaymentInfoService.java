package com.recarchoi.service;

import java.util.Map;

public interface PaymentInfoService {
    /**
     * 创建记录支付日志
     *
     * @param plainText 明文数据
     */
    void createPaymentInfo(String plainText);
}
