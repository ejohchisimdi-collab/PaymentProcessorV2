package com.chisimidi.account.service.models;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VaultDTO {
    private int id;
    private String token;
    private String currency;
    private AccountType accountType;
    private OwnerType ownerType;
    private String last4digits;
}
