package com.chisimidi.ledger.service.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LedgerDTO {
    private int id;
    private int paymentId;
    private int merchantId;
}
