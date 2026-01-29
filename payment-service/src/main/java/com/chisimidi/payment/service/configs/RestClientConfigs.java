package com.chisimidi.payment.service.configs;

import com.chisimidi.payment.service.services.JwtUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfigs {
    @Autowired
    JwtUtilService jwtUtilService;

    @Value("${user.service.url}")
    String userServiceUrl;

    @Value("${account.service.url}")
    String accountServiceUrl;

    @Bean
    @Qualifier("userClient")
    public RestClient userClient(){
        return RestClient.builder().defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(jwtUtilService.generateToken("Service",0,"Service"))).baseUrl(userServiceUrl).build();
    }
    @Bean
    @Qualifier("accountClient")
    public RestClient accountClient(){
        return RestClient.builder().defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(jwtUtilService.generateToken("Service",0,"Service"))).baseUrl(accountServiceUrl).build();
    }
}
