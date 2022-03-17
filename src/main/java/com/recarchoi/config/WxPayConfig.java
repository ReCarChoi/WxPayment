package com.recarchoi.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.*;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

@Data
@Slf4j
@Configuration
@PropertySource("classpath:wxpay.properties") //读取配置文件
@ConfigurationProperties(prefix = "wxpay") //读取wxpay节点
public class WxPayConfig {

    // 商户号
    private String mchId;

    // 商户API证书序列号
    private String mchSerialNo;

    // 商户私钥文件
    private String privateKeyPath;

    // APIv3密钥
    private String apiV3Key;

    // APPID
    private String appid;

    // 微信服务器地址
    private String domain;

    // 接收结果通知地址
    private String notifyDomain;

    /**
     * 获取商户私钥
     *
     * @param filename 文件路径
     * @return 商户私钥
     */
    private PrivateKey getPrivateKey(String filename) {
        try {
            return PemUtil.loadPrivateKey(
                    new FileInputStream("apiclient_key.pem"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("文件不存在", e);
        }
    }

    /**
     * 获取签名验证器
     *
     * @return Verifier对象
     */
    @Bean
    public Verifier getVerifier() throws GeneralSecurityException, IOException, HttpCodeException, NotFoundException {
        // 获取证书管理器实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();
        //商户密钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        //私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);
        //身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);
        // 向证书管理器增加需要自动更新平台证书的商户信息
        // ... 若有多个商户号，可继续调用putMerchant添加商户信息
        certificatesManager.putMerchant(
                mchId,
                new WechatPay2Credentials(mchId, privateKeySigner),
                apiV3Key.getBytes(StandardCharsets.UTF_8)
        );
        return certificatesManager.getVerifier(mchId);
    }

    /**
     * 获取http请求对象
     *
     * @param verifier 签名验证器
     * @return 请求对象
     */
    @Bean
    public CloseableHttpClient getWxPayClient(Verifier verifier) {
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, privateKey)
                .withValidator(new WechatPay2Validator(verifier));
        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
        return builder.build();
    }
}
