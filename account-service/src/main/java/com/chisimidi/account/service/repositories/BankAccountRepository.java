package com.chisimidi.account.service.repositories;

import com.chisimidi.account.service.models.Account;
import com.chisimidi.account.service.models.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,String> {
    Page<BankAccount> findByUserId(int userId, Pageable pageable);
}
