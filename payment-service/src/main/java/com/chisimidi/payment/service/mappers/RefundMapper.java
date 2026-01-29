package com.chisimidi.payment.service.mappers;

import com.chisimidi.payment.service.models.RefundDTO;
import com.chisimidi.payment.service.models.Refunds;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(source = "refunds.payment.id",target = "paymentId")
    RefundDTO toRefundDTO(Refunds refunds);

    List<RefundDTO> toRefundDTOList (List<Refunds>refunds);

}
