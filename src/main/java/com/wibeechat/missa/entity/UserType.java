package com.wibeechat.missa.entity;

import lombok.Getter;

@Getter
public enum UserType {
    UT001("대학생"),
    UT002("사회초년생"),
    UT003("신혼"),
    UT004("자녀영유아"),
    UT005("자녀의무교육"),
    UT006("자녀대학생"),
    UT007("중년기타"),
    UT008("재혼"),
    UT009("은퇴");

    private final String description;

    UserType(String description){
        this.description = description;
    }
}
