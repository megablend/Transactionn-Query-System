package com.nibss.tqs.config;

import com.nibss.tqs.controllers.CorporateLoungeApiController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by eoriarewo on 8/4/2017.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(CorporateLoungeApiController.class.getPackage().getName()))
                .paths(PathSelectors.regex("/corporateloungeapi.*"))
                .build()
                .apiInfo( apiInfo());

    }

    private ApiInfo apiInfo() {
        return new ApiInfo("Corporate Lounge REST API",
                "REST API to view realtime account balances on profiled accounts",
                "1.0",
                "", new Contact("Nigeria Inter-Bank Settlement System Plc.",
                "https://www.nibss-plc.com.ng" , "integration@nibss-plc.com.ng" ),
                "" , "");
    }

}
