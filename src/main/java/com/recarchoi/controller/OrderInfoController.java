package com.recarchoi.controller;

import com.recarchoi.entity.OrderInfo;
import com.recarchoi.enums.OrderStatus;
import com.recarchoi.service.OrderInfoService;
import com.recarchoi.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author recarchoi
 * @since 2022/3/20 22:20
 */
@Api(tags = "订单信息管理")
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/order-info")
public class OrderInfoController {

    private final OrderInfoService orderInfoService;

    @ApiOperation("获取订单列表")
    @GetMapping("/list")
    public Result getOrderInfoByList() {
        List<OrderInfo> list = orderInfoService.getListByCreateTimeDesc();
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", list);
        return Result.succ(map);
    }

    @ApiOperation("查询本地订单状态")
    @GetMapping("/query-order-status/{orderNo}")
    public Result queryOrderStatus(@PathVariable(value = "orderNo") String orderNo){
        String orderStatus = orderInfoService.getOrderStatusByOrderNo(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)){
            return Result.succ("支付成功");
        }
        return Result.succ(101,"支付中...");
    }

}
