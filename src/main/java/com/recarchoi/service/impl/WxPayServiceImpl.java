package com.recarchoi.service.impl;

import com.google.gson.Gson;
import com.recarchoi.config.WxPayConfig;
import com.recarchoi.entity.OrderInfo;
import com.recarchoi.enums.OrderStatus;
import com.recarchoi.enums.wxpay.WxApiType;
import com.recarchoi.enums.wxpay.WxNotifyType;
import com.recarchoi.service.OrderInfoService;
import com.recarchoi.service.PaymentInfoService;
import com.recarchoi.service.WxPayService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author recarchoi
 * @since 2022/3/17 22:52
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayServiceImpl implements WxPayService {

    private final WxPayConfig wxPayConfig;
    private final CloseableHttpClient wxPayClient;
    private final OrderInfoService orderInfoService;
    private final PaymentInfoService paymentInfoService;
    private final Lock lock = new ReentrantLock();

    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        //生成订单
        log.info("生成订单");
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrl = orderInfo.getCodeUrl();
        if (StringUtils.hasLength(codeUrl)) {
            log.info("订单已保存，二维码已存在");
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }
        //如果没有该订单，则生成新订单
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
            codeUrl = resultMap.get("code_url");
            //保存二维码
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);

            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());

            return map;
        } finally {
            response.close();
        }
    }

    @Override
    public void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("订单处理");
        //解密报文
        String plainText = decryptFromResource(bodyMap);
        //将明文转换成map
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        //修改订单状态
        String orderNo = (String) plainTextMap.get("out_trade_no");
        if (lock.tryLock()) {
            try {
                String orderStatus = orderInfoService.getOrderStatusByOrderNo(orderNo);
                if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
                    return;
                }
                orderInfoService.updateOrderStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                //记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 将加密的报文解密成明文
     *
     * @param bodyMap 存储加密报文的map
     * @return 明文
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("密文解密");
        //通知数据
        Map<String, String> resource = (Map<String, String>) bodyMap.get("resource");
        //附加数据
        String associatedData = resource.get("associated_data");
        //随机串
        String nonce = resource.get("nonce");
        //数据密文
        String ciphertext = resource.get("ciphertext");
        log.info("数据密文 ===> {}", ciphertext);
        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        String plainText = aesUtil.decryptToString(
                associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext
        );
        log.info("数据明文 ===> {}", plainText);
        return plainText;
    }
}
