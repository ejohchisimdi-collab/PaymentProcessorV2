package com.chisimidi.ledger.service.mappers;

import com.chisimidi.ledger.service.models.Refunds;
import com.chisimidi.ledger.service.models.RefundsDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefundsMapper {

    RefundsDTO toRefundsDTO(Refunds refunds);

    List<RefundsDTO>toRefundsDTOList(List<Refunds>refunds);
}
