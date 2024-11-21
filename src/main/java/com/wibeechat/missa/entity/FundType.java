package com.wibeechat.missa.entity;

import lombok.Getter;

@Getter
public enum FundType {
    FT001("채권형"),
    FT002("주식형"),
    FT003("혼합자산"),
    FT004("파생상품"),
    FT005("재간접");

    private final String description;

    FundType(String description) {
        this.description = description;
    }
}


