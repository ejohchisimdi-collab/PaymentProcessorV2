package com.chisimidi.account.service.mappers;

import com.chisimidi.account.service.models.CreditCard;
import com.chisimidi.account.service.models.CreditCardDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditCardMapper {
    @Mapping(target = "totalCreditRemaining", source = "moneyRemaining")
    CreditCardDTO toCreditCardDTO(CreditCard creditCard);
    List<CreditCardDTO>toCreditCardDTOList(List<CreditCard>creditCards);
}
