package com.chisimidi.account.service.repositories;

import com.chisimidi.account.service.models.OwnerType;
import com.chisimidi.account.service.models.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaultRepository extends JpaRepository<Vault,Integer> {
    Boolean existsByTokenAndOwnerType(String token, OwnerType ownerType);
    Vault findByToken(String token);
}
