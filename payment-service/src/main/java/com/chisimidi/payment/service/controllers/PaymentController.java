package com.chisimidi.payment.service.controllers;

import com.chisimidi.payment.service.models.PaymentDTO;
import com.chisimidi.payment.service.services.PaymentService;
import com.chisimidi.payment.service.utils.ProcessPaymentsUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class PaymentController {

    private PaymentService paymentService;

    @Operation(summary = "Creates a payment, requires hmac secret api key, available only to merchants")
    @PostMapping("/pay")
    public PaymentDTO pay(@RequestHeader("x-api-Key")String apiKey,@RequestHeader("Idempotency-Key")String idempotencyKey, @RequestBody @Valid ProcessPaymentsUtil paymentsUtil){
        return paymentService.processPayment(apiKey,idempotencyKey,paymentsUtil.getMerchantId(), paymentsUtil.getMerchantAccountToken(), paymentsUtil.getCustomerAccountToken(), paymentsUtil.getAmount());
    }

    @Operation(summary = "Manually capture a payment, available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @PostMapping("/capture/{paymentId}/merchants/{merchantId}")
    public PaymentDTO capture(@PathVariable("paymentId")int paymentId,@PathVariable("merchantId")int merchantId){
        return paymentService.manual(paymentId,merchantId);
    }

    @Operation(summary = "Retrieves payment by merchant Id, available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/payments/{merchantId}")
    public List<PaymentDTO>findByMerchantId(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return paymentService.findPaymentsByMerchant(merchantId, pageNumber, size);
    }

}
