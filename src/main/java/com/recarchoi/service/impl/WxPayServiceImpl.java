package com.recarchoi.service.impl;

import com.google.gson.Gson;
import com.recarchoi.config.WxPayConfig;
import com.recarchoi.entity.OrderInfo;
import com.recarchoi.enums.OrderStatus;
import com.recarchoi.enums.wxpay.WxApiType;
import com.recarchoi.enums.wxpay.WxNotifyType;
import com.recarchoi.service.WxPayService;
import com.recarchoi.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author recarchoi
 * @since 2022/3/17 22:52
 */
@Slf4j
@Service
public class WxPayServiceImpl implements WxPayService {
    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;

    /**
     * 创建订单，调用Native支付接口
     *
     * @param productId 产品Id
     * @return code_url 和 订单号
     * @throws Exception
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        //生成订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTitle("test");
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(1);
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        //实际业务需要判断用户
        //orderInfo.setUserId();
        //TODO：存入数据库

        log.info("调用统一生成API");
        //调用统一下单Api
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        //请求body参数
        Gson gson = new Gson();
        HashMap<Object, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));

        HashMap<Object, Object> amountMap = new HashMap<>();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);
        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数： " + jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();//响应状态
            if (statusCode == 200) { //处理成功
                log.info("success,return body = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("success");
            } else {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
                throw new IOException("request failed");
            }
            //响应结果
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            //二维码
            String codeUrl = resultMap.get("code_url");

            Map<String, Object> map = new HashMap<>();
            map.put("code_url", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());

            return map;
        } finally {
            response.close();
        }
    }
}