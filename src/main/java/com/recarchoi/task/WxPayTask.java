package com.recarchoi.task;

import com.recarchoi.entity.OrderInfo;
import com.recarchoi.service.OrderInfoService;
import com.recarchoi.service.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author recarchoi
 * @since 2022/3/27 19:12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WxPayTask {

    private final OrderInfoService orderInfoService;
    private final WxPayService wxPayService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() throws IOException {
        log.info("orderConfirm 被执行......");
        List<OrderInfo> orderInfos = orderInfoService.getNoPayOrderByDuration(1);
        for (OrderInfo orderInfo : orderInfos) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===> {}",orderNo);
            //核实订单状态，调用微信支付查单接口
            wxPayService.checkOrderStatus(orderNo);
        }
    }

}
