package com.chisimdi.user.service.models;

public class UserDTO {
    private int id;
    private String name;
    private String email;
    private String roles;
    private Boolean approved;

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return roles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
