package com.chisimidi.ledger.service.controllers;

import com.chisimidi.ledger.service.models.LedgerDTO;
import com.chisimidi.ledger.service.models.LedgerEntriesDTO;
import com.chisimidi.ledger.service.models.RefundsDTO;
import com.chisimidi.ledger.service.models.SplitDTO;
import com.chisimidi.ledger.service.services.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class LedgerController {
    public LedgerService ledgerService;

    @Operation(summary = "Retrieves all ledgers by payment")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/ledgers/payments/{paymentId}/merchants/{merchantId}")
    public LedgerDTO findLedgerByPayment(@PathVariable("paymentId")int paymentId,@PathVariable("merchantId")int merchantId){
        return ledgerService.findAllLedgersByPayment(merchantId,paymentId);
    }

    @Operation(summary = "Retrieves all ledger entries by payment")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/ledger-entries/payments/{paymentId}/merchants/{merchantId}")
    public List<LedgerEntriesDTO> findLedgerEntriesByPayment(@PathVariable("paymentId")int paymentId, @PathVariable("merchantId")int merchantId, @RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return ledgerService.findAllLedgerEntriesByPayment(merchantId,paymentId,pageNumber,size);
    }

    @Operation(summary = "Retrieves all refunds by payment")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/refunds/payments/{paymentId}/merchants/{merchantId}")
    public RefundsDTO findRefundsByPayment(@PathVariable("paymentId")int paymentId, @PathVariable("merchantId")int merchantId){
        return ledgerService.findRefundByPayment(paymentId, merchantId);
    }

    @Operation(summary = "Retrieves all splits by payment")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/splits/payments/{paymentId}/merchants/{merchantId}")
    public SplitDTO findSplitsByPayment(@PathVariable("paymentId")int paymentId, @PathVariable("merchantId")int merchantId){
        return ledgerService.findSplitByPayment(paymentId, merchantId);
    }

}
