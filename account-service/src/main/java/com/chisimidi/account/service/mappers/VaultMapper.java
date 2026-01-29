package com.chisimidi.account.service.mappers;

import com.chisimidi.account.service.models.Vault;
import com.chisimidi.account.service.models.VaultDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VaultMapper  {
    VaultDTO toVaultDTO(Vault vault);
    List<VaultDTO>toVaultDTOList(List<Vault>vaults);
}
