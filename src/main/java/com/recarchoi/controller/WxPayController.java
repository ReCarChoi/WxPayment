package com.recarchoi.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.recarchoi.service.WxPayService;
import com.recarchoi.util.HttpUtils;
import com.recarchoi.util.WechatPay2ValidatorForRequest;
import com.recarchoi.vo.Result;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
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
    private final Verifier verifier;

    @ApiOperation("调用统一Api，生成支付二维码")
    @PostMapping("/native/{productId}")
    public Result nativePay(@PathVariable(value = "productId") Long productId) throws Exception {
        log.info("发起支付请求");
        //返回支付二维码和链接
        Map<String, Object> map = wxPayService.nativePay(productId);
        return Result.succ(map);
    }

    @ApiOperation("支付结果通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();
        HashMap<String, String> responseMap = new HashMap<>();
        try {
            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            log.info("支付通知的id ===》 {}", bodyMap.get("id"));
            log.info("支付通知的完整信息 ===》 {}", body);

            //签名的验证
            WechatPay2ValidatorForRequest validatorForRequest = new WechatPay2ValidatorForRequest(
                    verifier,
                    body,
                    (String) bodyMap.get("id")
            );
            if (!validatorForRequest.validate(request)) {
                //验签应答
                response.setStatus(500);
                responseMap.put("code", "FAIL");
                responseMap.put("message", "签名验证失败");
                return gson.toJson(responseMap);
            }
            //TODO : 处理订单

            //应答超时
            //TimeUnit.SECONDS.sleep(5);
            //失败应答
            response.setStatus(200);
            responseMap.put("code", "SUCCESS");
            responseMap.put("message", "支付成功");
            return gson.toJson(responseMap);
        } catch (Exception e) {
            e.printStackTrace();
            //通知应答
            response.setStatus(500);
            responseMap.put("code", "FAIL");
            responseMap.put("message", "支付失败");
            return gson.toJson(responseMap);
        }
    }
}
