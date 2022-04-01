package com.recarchoi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recarchoi.entity.OrderInfo;
import com.recarchoi.enums.OrderStatus;

import java.util.List;

public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 通过产品Id创建订单信息
     *
     * @param productId 产品Id
     * @return 订单信息
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 存储订单二维码,默认有效期为2小时，
     * 实际开发要存入redis作为缓存
     *
     * @param orderNo 订单号
     * @param codeUrl codeUrl
     */
    void saveCodeUrl(String orderNo, String codeUrl);

    /**
     * 获取订单列表信息
     *
     * @return 订单列表
     */
    List<OrderInfo> getListByCreateTimeDesc();

    /**
     * 修改订单状态
     *
     * @param orderNo     订单号
     * @param orderStatus 需要修改成为的状态
     */
    void updateOrderStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    /**
     * 通过订单号获取订单状态
     *
     * @param orderNo 商户订单号
     * @return 订单状态
     */
    String getOrderStatusByOrderNo(String orderNo);

    /**
     * 获取未支付的订单
     * @param minutes 限定时间
     * @return 为支付订单列表
     */
    List<OrderInfo> getNoPayOrderByDuration(int minutes);

    /**
     * 通过订单编号获取订单
     * @param orderNo 订单编号
     * @return 订单
     */
    OrderInfo getOrderByOrderNo(String orderNo);
}
