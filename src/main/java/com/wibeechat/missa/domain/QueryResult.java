package com.wibeechat.missa.domain;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class QueryResult {
    private List<Double> values;

    public double getValue() {
        return values != null && !values.isEmpty() ? values.get(0) : 0.0;
    }

    public QueryResult() {
        this.values = new ArrayList<>();
        this.values.add(0.0);
    }

    public QueryResult(double value) {
        this.values = new ArrayList<>();
        this.values.add(value);
    }
}
