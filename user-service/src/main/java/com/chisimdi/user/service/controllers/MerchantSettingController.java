package com.chisimdi.user.service.controllers;

import com.chisimdi.user.service.models.CaptureType;
import com.chisimdi.user.service.models.MerchantSettingDTO;
import com.chisimdi.user.service.models.RefundType;
import com.chisimdi.user.service.services.MerchantSettingService;
import com.chisimdi.user.service.utils.CreateMerchantSettingUtil;
import com.chisimdi.user.service.utils.UpdateMerchantSettingUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.tomcat.util.buf.UEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequestMapping("/settings")
@RestController
public class MerchantSettingController {
    private MerchantSettingService merchantSettingService;

    public MerchantSettingController(MerchantSettingService merchantSettingService){
        this.merchantSettingService=merchantSettingService;
    }

    @Operation(summary = "Creates a merchant setting for a merchant")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantSettingUtil.merchantId")
    @PostMapping("/")
    public MerchantSettingDTO createMerchantSetting(@Valid @RequestBody CreateMerchantSettingUtil merchantSettingUtil){
        return merchantSettingService.createMerchantSetting(merchantSettingUtil.getMerchantId(), merchantSettingUtil.getCurrency(), merchantSettingUtil.getMerchantEndpoint(),merchantSettingUtil.getMaxTransactionLimit(),merchantSettingUtil.getCaptureType(),merchantSettingUtil.getRefundType() );
    }

    @Operation(summary = "Updates a merchant setting belonging to a merchant")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantSettingUtil.merchantId")
    @PutMapping("/")
    public MerchantSettingDTO updateMerchantSetting(@Valid @RequestBody UpdateMerchantSettingUtil merchantSettingUtil){
        return merchantSettingService.updateMerchantSetting(merchantSettingUtil.getMerchantId(), merchantSettingUtil.getCurrency(), merchantSettingUtil.getMerchantEndpoint(),merchantSettingUtil.getMaxTransactionLimit(),merchantSettingUtil.getCaptureType(),merchantSettingUtil.getRefundType());
    }

    @Operation(summary = "Finds merchant settings for a merchant")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @GetMapping("/{merchantId}")
    public MerchantSettingDTO findMerchantSetting(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.findMerchantSetting(merchantId);
    }

    @Operation(summary = "Verifies if merchant setting exists, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/exists")
    public Boolean doesMerchantSettingExist(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.doesMerchantSettingExist(merchantId);
    }

    @Operation(summary = "Retrieves the transaction limit located in merchant settings, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/transaction-limit")
    public BigDecimal getTransactionLimit(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.getTransactionLimit(merchantId);
    }

    @Operation(summary = "Retrieves the capture type  located in merchant settings, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/capture-type")
    public CaptureType getCaptureType(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.getMerchantCaptureType(merchantId);
    }


    @Operation(summary = "Retrieves the merchant endpoint located in merchant settings, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/merchant-endpoint")
    public String getMerchantEndpoint(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.getMerchantEndpoint(merchantId);
    }

    @Operation(summary = "Retrieves the currency located in merchant settings, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/currency")
    public String getMerchantCurrency(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.getMerchantCurrency(merchantId);
    }

    @Operation(summary = "Retrieves the refund type located in merchant settings, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/refund-type")
    public RefundType getMerchantRefundType(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.getMerchantRefundType(merchantId);
    }

    @Operation(summary = "Retrieves the hmac secret/ api key located in merchant settings, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{merchantId}/secret")
    public String getMerchantSecret(@PathVariable("merchantId")int merchantId){
        return merchantSettingService.getMerchantSecret(merchantId);
    }

}
