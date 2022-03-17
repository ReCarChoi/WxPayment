package com.recarchoi;

import com.recarchoi.config.WxPayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class PaymentApplicationTests {

    @Resource
    private WxPayConfig wxPayConfig;

    @Test
    void testGetPrivateKey() {
        //PrivateKey privateKey = wxPayConfig.getPrivateKey(wxPayConfig.getPrivateKeyPath());
        //System.out.println(privateKey);
    }

}
