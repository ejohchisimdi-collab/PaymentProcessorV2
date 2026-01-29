package com.chisimidi.account.service.configs;

import com.chisimidi.account.service.services.JwtUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Autowired
    JwtUtilService jwtUtilService;
    @Value("${user.service.url}")
    String userServiceUrl;

    @Qualifier("userClient")
    @Bean
    public RestClient userClient(){
        return RestClient.builder().defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(jwtUtilService.generateToken("Service",0,"Service"))).baseUrl(userServiceUrl).build();
    }
}
