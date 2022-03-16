package com.recarchoi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recarchoi.entity.Product;
import com.recarchoi.mapper.ProductMapper;
import com.recarchoi.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
