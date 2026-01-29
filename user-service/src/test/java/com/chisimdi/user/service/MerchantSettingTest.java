package com.chisimdi.user.service;

import com.chisimdi.user.service.mappers.MerchantSettingMapper;
import com.chisimdi.user.service.models.CaptureType;
import com.chisimdi.user.service.models.MerchantSetting;
import com.chisimdi.user.service.models.RefundType;
import com.chisimdi.user.service.models.User;
import com.chisimdi.user.service.repositories.MerchantSettingRepository;
import com.chisimdi.user.service.repositories.UserRepository;
import com.chisimdi.user.service.services.MerchantSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.UnknownCurrencyException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MerchantSettingTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private MerchantSettingRepository merchantSettingRepository;
    @Mock
    private MerchantSettingMapper merchantSettingMapper;
    @InjectMocks
    private MerchantSettingService merchantSettingService;

    @Test
    void createMerchantSettingTest(){
        int merchantId=1;
        String currency="USD";
        String merchantEndpoint="http://localhost:8080";
        BigDecimal maxTransactionLimit=BigDecimal.valueOf(5000);
        CaptureType captureType=CaptureType.AUTOMATIC;
        RefundType refundType=RefundType.COMPLETE;
        User user=new User();
        MerchantSetting merchantSetting=new MerchantSetting();
        ArgumentCaptor<MerchantSetting>captor=ArgumentCaptor.forClass(MerchantSetting.class);

        when(userRepository.findById(merchantId)).thenReturn(Optional.of(user));
        when(merchantSettingRepository.save(any(MerchantSetting.class))).thenReturn(captor.capture());

        merchantSettingService.createMerchantSetting(merchantId,currency,merchantEndpoint,maxTransactionLimit,captureType,refundType);

        verify(merchantSettingRepository).save(captor.capture());

        merchantSetting=captor.getValue();

        assertThat(merchantSetting.getCurrency()).isEqualTo("USD");

        verify(userRepository).findById(merchantId);

    }

    @Test
    void createMerchantSettingTest_ThrowsUnknownCurrencyException(){
        int merchantId=1;
        String currency="123";
        String merchantEndpoint="http://localhost:8080";
        BigDecimal maxTransactionLimit=BigDecimal.valueOf(5000);
        CaptureType captureType=CaptureType.AUTOMATIC;
        RefundType refundType=RefundType.COMPLETE;
        User user=new User();
        MerchantSetting merchantSetting=new MerchantSetting();



       assertThatThrownBy(()-> merchantSettingService.createMerchantSetting(merchantId,currency,merchantEndpoint,maxTransactionLimit,captureType,refundType)).isInstanceOf(UnknownCurrencyException.class);




        verify(userRepository,never()).findById(merchantId);

    }

    @Test
    void updateMerchantSettingTest(){
      int merchantId=1;
      MerchantSetting merchantSetting=new MerchantSetting();
      merchantSetting.setCaptureType(CaptureType.AUTOMATIC);

      when(merchantSettingRepository.findByMerchantId(merchantId)).thenReturn(merchantSetting);
 when(merchantSettingRepository.save(merchantSetting)).thenReturn(merchantSetting);

      merchantSettingService.updateMerchantSetting(merchantId,"USD",null,null,null,null);

      assertThat(merchantSetting.getCurrency()).isEqualTo("USD");
      assertThat(merchantSetting.getCaptureType()).isEqualTo(CaptureType.AUTOMATIC);

      verify(merchantSettingRepository).findByMerchantId(merchantId);
      verify(merchantSettingRepository).save(merchantSetting);
    }

    @Test
    void updateMerchantSetting_ThrowsUnknownCurrencyException(){
        int merchantId=1;
        MerchantSetting merchantSetting=new MerchantSetting();
        merchantSetting.setCaptureType(CaptureType.AUTOMATIC);

        when(merchantSettingRepository.findByMerchantId(merchantId)).thenReturn(merchantSetting);

      assertThatThrownBy(()->  merchantSettingService.updateMerchantSetting(merchantId,"123",null,null,null,null)).isInstanceOf(UnknownCurrencyException.class);


        verify(merchantSettingRepository).findByMerchantId(merchantId);
        verify(merchantSettingRepository,never()).save(merchantSetting);

    }

}
