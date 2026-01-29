package com.chisimidi.account.service.excptions;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ApiError2 {
    private int status;
    private String message;
    private LocalDateTime localDateTime;
    private ArrayList<String> error=new ArrayList<>();

    public ApiError2(int status,String message){
        this.status=status;
        this.message=message;
        this.localDateTime=LocalDateTime.now();
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<String> getError() {
        return error;
    }
}
