package com.chisimdi.user.service.utils;

import jakarta.validation.constraints.NotNull;

public class LoginRequest {
    @NotNull
    String userName;
    @NotNull
    String password;

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
