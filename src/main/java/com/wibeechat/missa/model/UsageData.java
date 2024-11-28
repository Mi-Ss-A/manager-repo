package com.wibeechat.missa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageData {

    @JsonProperty("object")
    private String object;

    @JsonProperty("data")
    private List<UsageItem> data = new ArrayList<>(); // 기본값 설정

    @JsonIgnore // JSON 변환 시 무시
    private String date; // 날짜 필드 추가
}
