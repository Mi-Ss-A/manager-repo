package com.wibeechat.missa.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RangeQueryResult {
    private List<DataPoint> dataPoints;

    @Data
    @AllArgsConstructor
    public static class DataPoint {
        private long timestamp;
        private double value;
    }

    public List<Double> getValues() {
        List<Double> values = dataPoints.stream()
                .map(DataPoint::getValue)
                .collect(Collectors.toList());

        // 24개가 되도록 부족한 만큼 0.0으로 채움
        while (values.size() < 24) {
            values.add(0.0);
        }
        return values;
    }
}