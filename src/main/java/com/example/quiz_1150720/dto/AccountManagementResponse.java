package com.example.quiz_1150720.dto;

public class AccountManagementResponse {

    private String account;
    private String userName;
    private Integer permissions;
    private Integer status;

    public AccountManagementResponse() {
    }

    public AccountManagementResponse(
            String account,
            String userName,
            Integer permissions,
            Integer status) {
        this.account = account;
        this.userName = userName;
        this.permissions = permissions;
        this.status = status;
    }

    public String getAccount() {
        return account;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getPermissions() {
        return permissions;
    }

    public Integer getStatus() {
        return status;
    }
}