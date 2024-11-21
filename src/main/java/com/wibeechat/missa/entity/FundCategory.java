package com.wibeechat.missa.entity;

import lombok.Getter;

@Getter
public enum FundCategory {

    FS063("자산운용사"),
    FS064("투자신탁"),
    FS065("은행"),
    FS067("증권"),
    FS068("기타금융"),
    FS071("외국계"),
    FS066("보험");

    private final String description;

    FundCategory(String description) {
        this.description = description;
    }
}
