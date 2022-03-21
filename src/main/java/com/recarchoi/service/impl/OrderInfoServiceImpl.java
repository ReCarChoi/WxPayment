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
import org.springframework.stereotype.Service;

import java.util.List;

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
                .eq("order_No", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);
        orderInfoMapper.update(orderInfo, wrapper);
    }

    @Override
    public List<OrderInfo> getListByCreateTimeDesc() {
        QueryWrapper<OrderInfo> orderInfoQueryWrapper = new QueryWrapper<OrderInfo>()
                .orderByDesc("create_time");
        return orderInfoMapper.selectList(orderInfoQueryWrapper);
    }

    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<OrderInfo>()
                .eq("product_id", productId)
                .eq("order_status", OrderStatus.NOTPAY.getType());
        //.eq("uesr_id",userId);
        return orderInfoMapper.selectOne(wrapper);
    }
}
