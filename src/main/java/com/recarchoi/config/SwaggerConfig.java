package com.recarchoi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * @author recarchoi
 * @since 2022/3/15 23:29
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo());
    }

    /**
     * Swagger信息的配置
     *
     * @return ApiInfo信息类
     */
    private ApiInfo apiInfo() {
        Contact contact = new Contact(
                "ReCarChoi",
                "https://www.recarchoi.xyz",
                "581652338@qq.com"
        );
        return new ApiInfo(
                "Blog Api Documentation",
                "接口文档",
                "1.0",
                "https://www.recarchoi.xyz",
                contact,
                "Apache 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList()
        );
    }
}
