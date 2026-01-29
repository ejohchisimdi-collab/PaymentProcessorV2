package com.chisimdi.user.service.mappers;

import com.chisimdi.user.service.models.MerchantSetting;
import com.chisimdi.user.service.models.MerchantSettingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MerchantSettingMapper {
    @Mapping(source = "merchantSetting.merchant.id",target = "merchantId")
    @Mapping(source = "id",target = "id")
    MerchantSettingDTO toMerchantSettingDTO(MerchantSetting merchantSetting);
}
