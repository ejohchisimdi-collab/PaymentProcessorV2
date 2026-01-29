package com.chisimidi.account.service.mappers;

import com.chisimidi.account.service.models.BankAccount;
import com.chisimidi.account.service.models.BankAccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    @Mapping(source = "moneyRemaining", target = "balance")
    BankAccountDTO toBankAccountDTO(BankAccount bankAccount);
    List<BankAccountDTO>toBankAccountDTOList(List<BankAccount>bankAccounts);
}
