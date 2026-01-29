package com.chisimidi.payment.service.controllers;

import com.chisimidi.payment.service.models.RefundDTO;
import com.chisimidi.payment.service.services.RefundService;
import com.chisimidi.payment.service.utils.ProcessRefundsUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class RefundsController {
    private RefundService refundService;

    @Operation(summary = "Creates a Refund, requires hmac secret api key, available only to merchants")
    @PostMapping("/refunds/")
    public RefundDTO processRefunds(@RequestHeader("x-api-Key")String apiKey,@RequestHeader("Idempotency-Key")String idempotencyKey, @Valid @RequestBody ProcessRefundsUtil refundsUtil){
        return refundService.processRefunds(apiKey,idempotencyKey,refundsUtil.getMerchantId(), refundsUtil.getPaymentId(), refundsUtil.getAmount());
    }

    @Operation(summary = "Retrieves all refunds by merchant Id, available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/refunds/{merchantId}")
    public List<RefundDTO>getAllMerchants(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return refundService.findRefundsByMerchant(merchantId, pageNumber, size);
    }
}
