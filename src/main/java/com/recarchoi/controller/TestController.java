package com.recarchoi.controller;

import com.recarchoi.config.WxPayConfig;
import com.recarchoi.vo.Result;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author recarchoi
 * @since 2022/3/17 9:28
 */
@Api(tags = "接口测试器")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Resource
    private WxPayConfig wxPayConfig;

    @GetMapping
    public Result getWxPayConfig() {
        String apiV3Key = wxPayConfig.getApiV3Key();
        return Result.succ(apiV3Key);
    }

}
