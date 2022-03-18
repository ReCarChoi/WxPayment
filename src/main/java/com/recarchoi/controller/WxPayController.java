package com.recarchoi.controller;

import com.recarchoi.service.WxPayService;
import com.recarchoi.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author recarchoi
 * @since 2022/3/17 22:50
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx-pay")
@Api(tags = "网站微信支付API")
public class WxPayController {

    private final WxPayService wxPayService;

    @ApiOperation("调用统一Api，生成支付二维码")
    @PostMapping("/native/{productId}")
    public Result nativePay(@PathVariable(value = "productId") Long productId) throws Exception {
        log.info("发起支付请求");
        //返回支付二维码和链接
        Map<String, Object> map = wxPayService.nativePay(productId);
        return Result.succ(map);
    }
}
