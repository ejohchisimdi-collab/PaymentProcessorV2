package com.chisimdi.webhook.service.configs;

import com.chisimdi.webhook.service.services.JwtUtilService;
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

    @Bean
    @Qualifier("userClient")
    public RestClient userClient(){
        return RestClient.builder().defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(jwtUtilService.generateToken("Service",0,"Service"))).baseUrl(userServiceUrl).build();
    }
}
