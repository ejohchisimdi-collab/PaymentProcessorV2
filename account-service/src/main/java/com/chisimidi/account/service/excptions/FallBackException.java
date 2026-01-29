package com.chisimidi.account.service.excptions;

public class FallBackException extends RuntimeException {
    public FallBackException(String message) {
        super(message);
    }
}
