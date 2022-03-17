package com.recarchoi.config;

import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.PrivateKey;

@Configuration
@PropertySource("classpath:wxpay.properties") //读取配置文件
@ConfigurationProperties(prefix = "wxpay") //读取wxpay节点
@Data //使用set方法将wxpay节点中的值填充到当前类的属性中
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

}
