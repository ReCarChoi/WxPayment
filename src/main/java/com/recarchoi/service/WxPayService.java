package com.recarchoi.service;

import java.io.IOException;
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
    Map<String, Object> nativePay(Long productId) throws IOException;

    /**
     * 订单处理
     *
     * @param bodyMap request当中返回的json数据转换成的map
     */
    void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException;

    /**
     * 通过订单号取消订单
     *
     * @param orderNo 订单号
     */
    void cancelOrderByOrderNo(String orderNo) throws IOException;

    /**
     * 查询符合条件的订单
     *
     * @param orderNo 订单号
     * @return 订单结果
     */
    String queryOrder(String orderNo) throws IOException;

    /**
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单已支付，则更新商户端订单状态
     * 如果订单未支付，则调用关单接口，并更新商户端订单状态
     *
     * @param orderNo 订单号
     */
    void checkOrderStatus(String orderNo) throws IOException;

    /**
     * 根据订单号退款该订单
     * @param orderNo 订单号
     * @param reason 退款理由
     */
    void refund(String orderNo, String reason) throws Exception;
}
