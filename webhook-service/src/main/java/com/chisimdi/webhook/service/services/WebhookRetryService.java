package com.chisimdi.webhook.service.services;

import com.chisimdi.webhook.service.models.WebhookDTO;
import com.chisimdi.webhook.service.models.WebhookRetries;
import com.chisimdi.webhook.service.repositories.WebhookRetriesRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
public class WebhookRetryService {
    private WebhookRetriesRepository webhookRetriesRepository;
    private RestClientService restClientService;
    private SendWebhookService sendWebhookService;

@Scheduled(cron = "0 0 * * * *")
    public void retryWebhooks()throws Exception{
    List<WebhookRetries>webhookRetries=webhookRetriesRepository.findByDoneAndNextRetryTimeBefore(false, LocalDateTime.now());
    for(WebhookRetries webhookRetry:webhookRetries){
       String merchantEndpoint=restClientService.getMerchantEndpoint(webhookRetry.getMerchantId());
       String hmacSecret=restClientService.getMerchantHMac(webhookRetry.getMerchantId());
        WebhookDTO webhookDTO=new WebhookDTO();
        webhookDTO.setStatus(webhookRetry.getStatus());
        webhookDTO.setAccountTo(webhookRetry.getAccountTo());
        webhookDTO.setAccountFrom(webhookRetry.getAccountFrom());
        webhookDTO.setPaymentId(webhookRetry.getPaymentId());
        webhookDTO.setMerchantId(webhookRetry.getMerchantId());
       try {
            sendWebhookService.sendWebhooks(merchantEndpoint,hmacSecret,webhookDTO);
        }
       catch (Exception e){
           long baseSeconds = 1;
           long capSeconds  = 30;

           long delaySeconds = Math.min(
                   capSeconds,
                   baseSeconds * (1L << webhookRetry.getRetryCount())
           );

           webhookRetry.setNextRetryTime(
                   webhookRetry.getNextRetryTime().plusSeconds(delaySeconds)
           );
           webhookRetry.setRetryCount(webhookRetry.getRetryCount()+1);
           if(webhookRetry.getRetryCount()==webhookRetry.getMaxRetries()){
               webhookRetry.setDone(true);
           }
           webhookRetriesRepository.save(webhookRetry);
       }
    }
}
}
