package com.chisimidi.ledger.service.mappers;

import com.chisimidi.ledger.service.models.Split;
import com.chisimidi.ledger.service.models.SplitDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SplitMapper {
    SplitDTO toSplitDTO(Split split);
    List<SplitDTO>toSplitDTOList(List<Split>splits);

}
