package com.chisimdi.webhook.service.services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@AllArgsConstructor
@Service
public class RestClientService {
    @Qualifier("userClient")
    RestClient userClient;

    public String getMerchantEndpoint(int merchantId){
        return userClient.get().uri("/settings/{id}/merchant-endpoint",merchantId).retrieve().body(String.class);
    }

    public String getMerchantHMac(int merchantId){
        return userClient.get().uri("/settings/{id}/secret",merchantId).retrieve().body(String.class);
    }
}
