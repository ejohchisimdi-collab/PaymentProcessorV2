package com.chisimidi.ledger.service.mappers;

import com.chisimidi.ledger.service.models.LedgerEntries;
import com.chisimidi.ledger.service.models.LedgerEntriesDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LedgerEntriesMapper {

    LedgerEntriesDTO toLedgerEntriesDTO(LedgerEntries ledgerEntries);

    List<LedgerEntriesDTO>toLedgerEntriesDTO(List<LedgerEntries>ledgerEntries);
}
