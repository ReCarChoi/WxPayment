package com.recarchoi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recarchoi.entity.RefundInfo;

public interface RefundInfoService extends IService<RefundInfo> {

    /**
     * 根据订单号和理由创建退款订单
     *
     * @param orderNo 订单号
     * @param reason  退款理由
     * @return 退款订单记录
     */
    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    /**
     * 记录退款记录
     * @param content 响应结果
     */
    void updateRefund(String content);
}
