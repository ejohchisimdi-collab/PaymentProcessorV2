package com.chisimidi.payment.service.models;

public enum PaymentStatus {
    CREDIT_PENDING,
    VALIDATED,
    AUTHORISED,
    CAPTURED,
    SETTLED,
    FAILED,
    BANK_PENDING
}
