package com.chisimdi.webhook.service.services;

import com.chisimdi.payment.events.AuthorizationEvent;
import com.chisimdi.payment.events.CaptureEvent;
import com.chisimdi.payment.events.RefundCompletedEvent;
import com.chisimdi.payment.events.SettlementEvent;
import com.chisimdi.webhook.service.models.EventsIdempotency;
import com.chisimdi.webhook.service.models.Status;
import com.chisimdi.webhook.service.models.WebhookDTO;
import com.chisimdi.webhook.service.models.WebhookRetries;
import com.chisimdi.webhook.service.repositories.EventsIdempotencyRepository;
import com.chisimdi.webhook.service.repositories.WebhookRetriesRepository;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class WebhookEventsService {
    private WebhookRetriesRepository webhookRetriesRepository;
    private SendWebhookService sendWebhookService;
    private RestClientService restClientService;
    private EventsIdempotencyRepository idempotencyRepository;

    @KafkaListener(topics = "authorization-completed")
    public void sendAuthorisedWebHook(AuthorizationEvent authorizationEvent)throws Exception{
        EventsIdempotency eventsIdempotency=idempotencyRepository.findById(authorizationEvent.getId()).orElse(null);
        if(eventsIdempotency!=null){
            return;
        }

        String merchantUrl=restClientService.getMerchantEndpoint(authorizationEvent.getMerchantId());
        String hmacSecret=restClientService.getMerchantHMac(authorizationEvent.getMerchantId());

        WebhookDTO webhookDTO=new WebhookDTO();
        webhookDTO.setStatus(Status.PAYMENT_AUTHORISED);
        webhookDTO.setAccountFrom(authorizationEvent.getAccountFrom());
        webhookDTO.setAmount(authorizationEvent.getAmount());
        webhookDTO.setMerchantId(authorizationEvent.getMerchantId());
        webhookDTO.setAccountTo(authorizationEvent.getAccountTo());
        webhookDTO.setPaymentId(authorizationEvent.getPaymentId());

        try {
            sendWebhookService.sendWebhooks(merchantUrl,hmacSecret,webhookDTO);
        }
        catch (Exception e){
            WebhookRetries webhookRetries=new WebhookRetries();
            webhookRetries.setAccountFrom(webhookDTO.getAccountFrom());
            webhookRetries.setAccountTo(webhookDTO.getAccountTo());
            webhookRetries.setAmount(webhookDTO.getAmount());
            webhookRetries.setStatus(webhookDTO.getStatus());
            webhookRetries.setMerchantId(webhookDTO.getMerchantId());
            webhookRetries.setPaymentId(webhookDTO.getPaymentId());
            webhookRetriesRepository.save(webhookRetries);
        }
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(authorizationEvent.getId());
        idempotency.setContext("Authorization Event");
        idempotencyRepository.save(idempotency);


    }

@KafkaListener(topics = "payment-captured")
    public void sendCapturedWebhooks(CaptureEvent captureEvent){
    EventsIdempotency eventsIdempotency=idempotencyRepository.findById(captureEvent.getId()).orElse(null);
    if(eventsIdempotency!=null){
        return;
    }

        String merchantUrl=restClientService.getMerchantEndpoint(captureEvent.getMerchantId());
        String hmacSecret=restClientService.getMerchantHMac(captureEvent.getMerchantId());

        WebhookDTO webhookDTO=new WebhookDTO();
        webhookDTO.setAccountFrom(captureEvent.getCustomerAccount());
        webhookDTO.setAmount(captureEvent.getTotalAmount());
        webhookDTO.setMerchantId(captureEvent.getMerchantId());
        webhookDTO.setAccountTo(captureEvent.getMerchantAccount());
        webhookDTO.setStatus(Status.PAYMENT_CAPTURED);
        webhookDTO.setPaymentId(captureEvent.getPaymentId());

        try {
            sendWebhookService.sendWebhooks(merchantUrl,hmacSecret,webhookDTO);
        }
        catch (Exception e){
            WebhookRetries webhookRetries=new WebhookRetries();
            webhookRetries.setAccountFrom(webhookDTO.getAccountFrom());
            webhookRetries.setAccountTo(webhookDTO.getAccountTo());
            webhookRetries.setAmount(webhookDTO.getAmount());
            webhookRetries.setMerchantId(webhookDTO.getMerchantId());
            webhookRetries.setPaymentId(webhookDTO.getPaymentId());
            webhookRetries.setStatus(webhookDTO.getStatus());
            webhookRetriesRepository.save(webhookRetries);
        }
    EventsIdempotency idempotency=new EventsIdempotency();
    idempotency.setId(captureEvent.getId());
    idempotency.setContext("Capture Event");
    idempotencyRepository.save(idempotency);


    }
    @KafkaListener(topics = "payment-settled")
    public void sendSettledWebhooks(SettlementEvent settlementEvent){
        EventsIdempotency eventsIdempotency=idempotencyRepository.findById(settlementEvent.getId()).orElse(null);
        if(eventsIdempotency!=null){
            return;
        }

        String merchantUrl=restClientService.getMerchantEndpoint(settlementEvent.getMerchantId());
        String hmacSecret=restClientService.getMerchantHMac(settlementEvent.getMerchantId());

        WebhookDTO webhookDTO=new WebhookDTO();
        webhookDTO.setStatus(Status.PAYMENT_SETTLED);
        webhookDTO.setAccountFrom(settlementEvent.getCustomerToken());
        webhookDTO.setAmount(settlementEvent.getTotalAmount());
        webhookDTO.setMerchantId(settlementEvent.getMerchantId());
        webhookDTO.setAccountTo(settlementEvent.getMerchantToken());
        webhookDTO.setPaymentId(settlementEvent.getPaymentId());

        try {
            sendWebhookService.sendWebhooks(merchantUrl,hmacSecret,webhookDTO);
        }
        catch (Exception e){
            WebhookRetries webhookRetries=new WebhookRetries();
            webhookRetries.setAccountFrom(webhookDTO.getAccountFrom());
            webhookRetries.setAccountTo(webhookDTO.getAccountTo());
            webhookRetries.setAmount(webhookDTO.getAmount());
            webhookRetries.setMerchantId(webhookDTO.getMerchantId());
            webhookRetries.setPaymentId(webhookDTO.getPaymentId());
            webhookRetries.setStatus(webhookDTO.getStatus());
            webhookRetriesRepository.save(webhookRetries);
        }
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(settlementEvent.getId());
        idempotency.setContext("Settlement Event");
        idempotencyRepository.save(idempotency);
    }

    @KafkaListener(topics = "refund-completed")
    public void sendRefundCompletedWebhooks(RefundCompletedEvent refundCompletedEvent){String merchantUrl=restClientService.getMerchantEndpoint(refundCompletedEvent.getMerchantId());
        EventsIdempotency eventsIdempotency=idempotencyRepository.findById(refundCompletedEvent.getId()).orElse(null);
        if(eventsIdempotency!=null){
            return;
        }

        String hmacSecret=restClientService.getMerchantHMac(refundCompletedEvent.getMerchantId());

        WebhookDTO webhookDTO=new WebhookDTO();
        webhookDTO.setStatus(Status.PAYMENT_SETTLED);
        webhookDTO.setAccountFrom(refundCompletedEvent.getCustomerAccountToken());
        webhookDTO.setAmount(refundCompletedEvent.getTotalAmount());
        webhookDTO.setMerchantId(refundCompletedEvent.getMerchantId());
        webhookDTO.setAccountTo(refundCompletedEvent.getMerchantToken());
        webhookDTO.setPaymentId(refundCompletedEvent.getPaymentId());

        try {
            sendWebhookService.sendWebhooks(merchantUrl,hmacSecret,webhookDTO);
        }
        catch (Exception e){
            WebhookRetries webhookRetries=new WebhookRetries();
            webhookRetries.setAccountFrom(webhookDTO.getAccountFrom());
            webhookRetries.setAccountTo(webhookDTO.getAccountTo());
            webhookRetries.setAmount(webhookDTO.getAmount());
            webhookRetries.setStatus(webhookDTO.getStatus());
            webhookRetries.setMerchantId(webhookDTO.getMerchantId());
            webhookRetries.setPaymentId(webhookDTO.getPaymentId());
            webhookRetriesRepository.save(webhookRetries);
        }
        EventsIdempotency idempotency=new EventsIdempotency();
        idempotency.setId(refundCompletedEvent.getId());
        idempotency.setContext("Refund completed Event");
        idempotencyRepository.save(idempotency);
    }


}
