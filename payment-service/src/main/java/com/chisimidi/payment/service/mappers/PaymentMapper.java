package com.chisimidi.payment.service.mappers;

import com.chisimidi.payment.service.models.Payment;
import com.chisimidi.payment.service.models.PaymentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "amountAfterConversion",target = "amountAfterConversion")
    PaymentDTO toPaymentDTO(Payment payment);
    List<PaymentDTO>toPaymentDTOList(List<Payment>payments);
}
