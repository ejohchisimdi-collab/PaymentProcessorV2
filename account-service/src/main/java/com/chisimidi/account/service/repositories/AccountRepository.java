package com.chisimidi.account.service.repositories;

import com.chisimidi.account.service.models.Account;
import com.chisimidi.account.service.models.OwnerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account,String> {
    Page<Account>findByUserId(int userId, Pageable pageable);
    Boolean existsByAccountNumberAndUserIdAndOwnerType(String id, int merchantId, OwnerType ownerType);
}
