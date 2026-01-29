package com.chisimdi.user.service.services;

import com.chisimdi.user.service.exceptions.ResourceNotFoundException;
import com.chisimdi.user.service.mappers.MerchantSettingMapper;
import com.chisimdi.user.service.models.*;
import com.chisimdi.user.service.repositories.MerchantSettingRepository;
import com.chisimdi.user.service.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MerchantSettingService {
    private UserRepository userRepository;
    private MerchantSettingRepository merchantSettingRepository;
private MerchantSettingMapper merchantSettingMapper;

    public MerchantSettingService(UserRepository userRepository,MerchantSettingRepository merchantSettingRepository,MerchantSettingMapper merchantSettingMapper){
        this.userRepository=userRepository;
        this.merchantSettingRepository=merchantSettingRepository;
        this.merchantSettingMapper=merchantSettingMapper;
    }

    @Transactional
    public MerchantSettingDTO createMerchantSetting(int merchantId,String currency, String merchantEndpoint, BigDecimal maxTransactionLimit, CaptureType captureType,
     RefundType refundType){
        CurrencyUnit currencyUnit= Monetary.getCurrency(currency);
        byte[] key = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        String hmacSecret=Base64.getEncoder().encodeToString(key);
        User merchant=userRepository.findById(merchantId).orElseThrow(()->new ResourceNotFoundException("merchant with Id "+merchantId+" not found"));
        MerchantSetting merchantSetting=new MerchantSetting();
        merchantSetting.setMerchant(merchant);
        merchantSetting.setMerchantEndpoint(merchantEndpoint);
        merchantSetting.setCaptureType(captureType);
        merchantSetting.setCurrency(currency);
        merchantSetting.setRefundType(refundType);
        merchantSetting.setMaxTransactionLimit(maxTransactionLimit);
        merchantSetting.setHmacSecret(hmacSecret);
        merchantSettingRepository.save(merchantSetting);
        return merchantSettingMapper.toMerchantSettingDTO(merchantSetting);
    }

    @Transactional
    public MerchantSettingDTO updateMerchantSetting(int merchantId,String currency, String merchantEndpoint, BigDecimal maxTransactionLimit, CaptureType captureType,
                                                    RefundType refundType){
        MerchantSetting merchantSetting=merchantSettingRepository.findByMerchantId(merchantId);
        if(currency!=null){
        CurrencyUnit currencyUnit= Monetary.getCurrency(currency);
        }
        if(merchantSetting==null){
            throw new ResourceNotFoundException("Merchant Setting with id "+merchantId+" not found");
        }
        if(merchantEndpoint!=null) {
            merchantSetting.setMerchantEndpoint(merchantEndpoint);
        }
        if(maxTransactionLimit!=null) {
            merchantSetting.setMaxTransactionLimit(maxTransactionLimit);
        }
        if(captureType!=null) {
            merchantSetting.setCaptureType(captureType);
        }
        if(refundType!=null) {
            merchantSetting.setRefundType(refundType);
        }
        if(currency!=null) {
            merchantSetting.setCurrency(currency);
        }
        merchantSettingRepository.save(merchantSetting);

        return merchantSettingMapper.toMerchantSettingDTO(merchantSetting);
    }

    public MerchantSettingDTO findMerchantSetting(int merchantId){
        MerchantSetting merchantSetting=merchantSettingRepository.findByMerchantId(merchantId);
        return merchantSettingMapper.toMerchantSettingDTO(merchantSetting);
    }

    public Boolean doesMerchantSettingExist(int merchantId){
        return merchantSettingRepository.existsByMerchantId(merchantId);
    }
    public String getMerchantCurrency(int merchantId){
        return merchantSettingRepository.findByMerchantId(merchantId).getCurrency();
    }
    public BigDecimal getTransactionLimit(int merchantId){
        return merchantSettingRepository.findByMerchantId(merchantId).getMaxTransactionLimit();
    }
    public CaptureType getMerchantCaptureType(int merchantId){
        return merchantSettingRepository.findByMerchantId(merchantId).getCaptureType();
    }
    public String getMerchantEndpoint(int merchantId){
        return merchantSettingRepository.findByMerchantId(merchantId).getMerchantEndpoint();
    }
    public RefundType getMerchantRefundType(int merchantId){
        return merchantSettingRepository.findByMerchantId(merchantId).getRefundType();
    }
    public String getMerchantSecret(int merchantId){
        return merchantSettingRepository.findByMerchantId(merchantId).getHmacSecret();
    }
}
