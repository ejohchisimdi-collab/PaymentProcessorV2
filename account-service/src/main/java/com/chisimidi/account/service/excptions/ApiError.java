package com.chisimidi.account.service.excptions;

import java.time.LocalDateTime;

public class ApiError {
    private int status;
    private String error;
    private LocalDateTime localDateTime;

    public ApiError(int status,String error){
        this.status=status;
        this.error=error;
        this.localDateTime=LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public String getError() {
        return error;
    }
}
