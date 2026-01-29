package com.chisimidi.account.service.services;

import com.chisimidi.account.service.excptions.FallBackException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@AllArgsConstructor
@Service
public class RestClientService {
    @Qualifier("userClient")
    private RestClient restClient;

    @CircuitBreaker(name = "userService",fallbackMethod = "userServiceFallBack")
    public Boolean doesUserExist(int userId){
        return restClient.get().uri("/users/{userId}/exists",userId).retrieve().body(Boolean.class);
    }

    public Boolean userServiceFallBack(int userId,Throwable t){
        throw new FallBackException("User profiles not available");
    }


}
