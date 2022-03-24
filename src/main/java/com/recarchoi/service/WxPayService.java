package com.recarchoi.service;

import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @author recarchoi
 * @since 2022/3/17 22:51
 */
public interface WxPayService {
    /**
     * 调用统一Api，生成支付二维码
     *
     * @param productId 产品Id
     * @return 支付对象
     */
    Map<String, Object> nativePay(Long productId) throws Exception;

    /**
     * 订单处理
     *
     * @param bodyMap request当中返回的json数据转换成的map
     */
    void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException;
}
