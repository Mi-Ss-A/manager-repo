package com.wibeechat.missa.entity;


import lombok.Getter;

@Getter
public enum UserStatus {
    A("ACTIVE"),
    I("INACTIVE"),
    D("DELETED");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}