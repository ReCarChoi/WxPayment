package com.recarchoi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.recarchoi.entity.PaymentInfo;
import com.recarchoi.enums.PayType;
import com.recarchoi.mapper.PaymentInfoMapper;
import com.recarchoi.service.PaymentInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    private final PaymentInfoMapper paymentInfoMapper;

    @Override
    public void createPaymentInfo(String plainText) {
        log.info("记录支付日志");
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        //商户订单号
        String orderNo = (String) plainTextMap.get("out_trade_no");
        //微信支付订单号
        String transactionId = (String) plainTextMap.get("transaction_id");
        //交易类型
        String tradeType = (String) plainTextMap.get("trade_type");
        //交易状态
        String tradeState = (String) plainTextMap.get("trade_state");
        //用户支付金额
        Map<String, Object> amount = (Map<String, Object>) plainTextMap.get("amount");
        BigDecimal payerTotal = BigDecimal.valueOf((Double) amount.get("payer_total"));
        int intPayerTotal = payerTotal.intValue();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(intPayerTotal);
        paymentInfo.setContent(plainText);
        paymentInfoMapper.insert(paymentInfo);
    }
}
