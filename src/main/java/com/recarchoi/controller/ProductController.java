package com.recarchoi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recarchoi.entity.Product;
import com.recarchoi.service.ProductService;
import com.recarchoi.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * @author recarchoi
 * @since 2022/3/15 23:36
 */
@Api(tags = "商品管理")
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/product")
public class ProductController {

    public final ProductService productService;

    @GetMapping("/list")
    @ApiOperation("获取商品列表")
    public Result list() {
        System.out.println(productService);
        List<Product> products = productService.list(new QueryWrapper<Product>());
        HashMap<String, Object> map = new HashMap<>();
        map.put("productList", products);
        return Result.succ(map);
    }

}
