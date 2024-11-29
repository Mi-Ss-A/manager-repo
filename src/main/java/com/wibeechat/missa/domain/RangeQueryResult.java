package com.wibeechat.missa.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class RangeQueryResult {
    private List<DataPoint> data;

    @Getter
    @AllArgsConstructor
    public static class DataPoint {
        private long timestamp;
        private double value;
    }

    public RangeQueryResult(List<DataPoint> data) {
        this.data = data;
    }

    public List<DataPoint> getData() {
        return data != null ? data : Collections.emptyList();
    }
}