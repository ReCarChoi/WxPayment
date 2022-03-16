package com.recarchoi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recarchoi.entity.Product;
import com.recarchoi.service.ProductService;
import com.recarchoi.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author recarchoi
 * @since 2022/3/15 23:36
 */
@Api(tags = "商品管理")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/product")
public class ProductController {

    public final ProductService productService;

    @GetMapping("/list")
    @ApiOperation("获取商品列表")
    public Result list(){
        System.out.println(productService);
        List<Product> products = productService.list(new QueryWrapper<Product>());
        return Result.succ(products);
    }

}
