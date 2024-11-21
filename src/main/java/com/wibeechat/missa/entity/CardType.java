package com.wibeechat.missa.entity;

import lombok.Getter;

@Getter
public enum CardType {
    CD001("신용"),
    CD002("체크"),
    CD003("소액"),
    CD004("하이브리드");

    private final String description;

    CardType(String description) {
        this.description = description;
    }
}
