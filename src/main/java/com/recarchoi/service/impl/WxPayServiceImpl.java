package com.recarchoi.service.impl;

import com.google.gson.Gson;
import com.recarchoi.config.WxPayConfig;
import com.recarchoi.entity.OrderInfo;
import com.recarchoi.entity.RefundInfo;
import com.recarchoi.enums.OrderStatus;
import com.recarchoi.enums.wxpay.WxApiType;
import com.recarchoi.enums.wxpay.WxNotifyType;
import com.recarchoi.enums.wxpay.WxTradeState;
import com.recarchoi.service.OrderInfoService;
import com.recarchoi.service.PaymentInfoService;
import com.recarchoi.service.RefundInfoService;
import com.recarchoi.service.WxPayService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
    private final RefundInfoService refundInfoService;
    private final Lock lock = new ReentrantLock();
    private final Gson gson = new Gson();

    @Override
    public Map<String, Object> nativePay(Long productId) throws IOException {
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
        //Gson gson = new Gson();
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
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost)) {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();//响应状态
            response.close();
            if (statusCode == 200) { //处理成功
                log.info("success,return body = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("success");
            } else {
                log.info("failed,resp code = " + statusCode + ",return body = " + bodyAsString);
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
        }
    }

    @Override
    public void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("订单处理");
        //解密报文
        String plainText = decryptFromResource(bodyMap);
        //将明文转换成map
        //Gson gson = new Gson();
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

    @Override
    public void cancelOrderByOrderNo(String orderNo) throws IOException {
        //调用微信支付的关单接口
        this.closeOrder(orderNo);
        //更新商户端的订单状态
        orderInfoService.updateOrderStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    @Override
    public String queryOrder(String orderNo) throws IOException {
        log.info("查单接口调用 ===> {}", orderNo);
        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        //完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpGet)) {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();//响应状态码
            response.close();
            if (statusCode == 200) { //处理成功
                log.info("成功, 返回结果 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("查单接口调用,响应码 = " + statusCode + ",返回结果 = " + bodyAsString);
                throw new IOException("request failed");
            }
            return bodyAsString;
        }
    }

    @Override
    public void checkOrderStatus(String orderNo) throws IOException {
        log.warn("根据订单号核实订单状态 ===> {}", orderNo);
        //调用微信支付查单接口
        String bodyAsString = this.queryOrder(orderNo);
        Map resultMap = gson.fromJson(bodyAsString, HashMap.class);
        //获取微信支付端的订单状态
        String orderStatus = (String) resultMap.get("trade_state");
        //判断订单状态
        if (WxTradeState.SUCCESS.getType().equals(orderStatus)) {
            log.warn("核实订单已支付 ===> {}", orderNo);
            //如果确认已支付，更新本地订单状态
            orderInfoService.updateOrderStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            //纪律支付日志
            paymentInfoService.createPaymentInfo(bodyAsString);
        }
        if (WxTradeState.NOTPAY.getType().equals(orderStatus)) {
            log.warn("订单未支付 ===> {}", orderNo);
            //如果订单未支付，则调用关单接口
            this.closeOrder(orderNo);
            //更新本地订单状态
            orderInfoService.updateOrderStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    @Override
    public void refund(String orderNo, String reason) throws Exception{
        log.info("创建退款单记录");
        //根据订单表号创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNo(orderNo,reason);
        //调用退款API
        log.info("调用退款API");

        //调用统一下单API
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);

        // 请求body参数
        Gson gson = new Gson();
        Map paramsMap = new HashMap();
        paramsMap.put("out_trade_no", orderNo);//订单编号
        paramsMap.put("out_refund_no", refundInfo.getRefundNo());//退款单编号
        paramsMap.put("reason", reason);//退款原因
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));//退款通知地址

        Map amountMap = new HashMap();
        amountMap.put("refund", refundInfo.getRefund());//退款金额
        amountMap.put("total", refundInfo.getTotalFee());//原订单金额
        amountMap.put("currency", "CNY");//退款币种
        paramsMap.put("amount", amountMap);

        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}" + jsonParams);
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");//设置请求报文格式
        httpPost.setEntity(entity);//将请求报文放入请求对象
        httpPost.setHeader("Accept", "application/json");//设置响应报文格式
        //完成签名并执行请求，并完成验签
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost)) {
            //解析响应结果
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            response.close();
            if (statusCode == 200) {
                log.info("成功, 退款返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("退款异常, 响应码 = " + statusCode + ", 退款返回结果 = " + bodyAsString);
            }
            //更新订单状态
            orderInfoService.updateOrderStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);
            //更新退款单
            refundInfoService.updateRefund(bodyAsString);
        }
    }

    /**
     * 调用微信支付的关单接口
     *
     * @param orderNo 订单号
     */
    private void closeOrder(String orderNo) throws IOException {
        log.info("关单接口的调用，订单号 ===> {}", orderNo);
        //创建远程请求对象
        String url = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost httpPost = new HttpPost(url);

        //组装json请求体
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String paramsJson = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}", paramsJson);

        //将请求参数设置到请求对象中
        StringEntity entity = new StringEntity(paramsJson, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();//响应状态码
            response.close();
            if (statusCode == 200) { //处理成功
                log.info("成功200");
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功204");
            } else {
                log.info("Native下单失败,响应码 = " + statusCode);
                throw new IOException("request failed");
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
