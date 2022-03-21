package com.recarchoi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recarchoi.entity.OrderInfo;

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
}
