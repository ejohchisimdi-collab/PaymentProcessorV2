package com.chisimidi.ledger.service.mappers;

import com.chisimidi.ledger.service.models.Ledger;
import com.chisimidi.ledger.service.models.LedgerDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LedgerMapper {

    LedgerDTO toLedgerDTO(Ledger ledger);

    List<LedgerDTO>toLedgerDTOList(List<Ledger>ledgers);
}
