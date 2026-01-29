package com.chisimdi.user.service.repositories;

import com.chisimdi.user.service.models.MerchantSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantSettingRepository extends JpaRepository<MerchantSetting,Integer> {
   MerchantSetting findByMerchantId(int merchantId);
   Boolean existsByMerchantId(int merchantId);
}
