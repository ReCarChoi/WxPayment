package com.recarchoi.controller;

import com.recarchoi.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author recarchoi
 * @since 2022/3/15 23:36
 */
@Api(tags = "商品管理")
@RestController
@RequestMapping(value = "/product")
public class ProductController {

    @ApiOperation(value = "接口测试")
    @GetMapping("/test")
    public Result test(){
        return Result.succ("测试成功");
    }

}
