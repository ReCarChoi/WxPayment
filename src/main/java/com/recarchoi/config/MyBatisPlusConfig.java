package com.recarchoi.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author recarchoi
 * @since 2022/3/16 12:41
 */
@Configuration
@MapperScan("com.recarchoi.mapper")
@EnableTransactionManagement
public class MyBatisPlusConfig {
}
