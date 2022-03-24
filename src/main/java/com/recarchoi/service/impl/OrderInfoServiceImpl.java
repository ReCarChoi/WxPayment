package com.recarchoi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recarchoi.entity.OrderInfo;
import com.recarchoi.entity.Product;
import com.recarchoi.enums.OrderStatus;
import com.recarchoi.mapper.OrderInfoMapper;
import com.recarchoi.mapper.ProductMapper;
import com.recarchoi.service.OrderInfoService;
import com.recarchoi.util.OrderNoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    private final ProductMapper productMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    public OrderInfo createOrderByProductId(Long productId) {
        OrderInfo orderInfo = getNoPayOrderByProductId(productId);
        if (orderInfo != null) {
            return orderInfo;
        }
        //获取商品信息
        Product product = productMapper.selectById(productId);

        //生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(1);
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        //实际业务需要判断用户
        //orderInfo.setUserId();

        orderInfoMapper.insert(orderInfo);
        return orderInfo;
    }

    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("order_no", orderNo);
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        orderInfo.setCodeUrl(codeUrl);
        orderInfoMapper.update(orderInfo, wrapper);
    }

    @Override
    public List<OrderInfo> getListByCreateTimeDesc() {
        QueryWrapper<OrderInfo> orderInfoQueryWrapper = new QueryWrapper<OrderInfo>()
                .orderByDesc("create_time");
        return orderInfoMapper.selectList(orderInfoQueryWrapper);
    }

    @Override
    public void updateOrderStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        log.info("更新订单状态 ===> {}", orderStatus.getType());
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("order_no", orderNo);
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        orderInfo.setOrderStatus(orderStatus.getType());
        orderInfoMapper.update(orderInfo, wrapper);
    }

    @Override
    public String getOrderStatusByOrderNo(String orderNo) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("order_no", orderNo);
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        if (orderInfo == null) {
            log.info("不存在该订单");
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("product_id", productId)
                .eq("order_status", OrderStatus.NOTPAY.getType());
        //.eq("uesr_id",userId);
        return orderInfoMapper.selectOne(wrapper);
    }
}
