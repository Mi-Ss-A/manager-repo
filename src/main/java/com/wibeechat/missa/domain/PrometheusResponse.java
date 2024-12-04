package com.wibeechat.missa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrometheusResponse {
    private String status;
    private Data data;
    private double value;

    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
        if (data != null && data.getResult() != null && !data.getResult().isEmpty()) {
            // Prometheus는 value를 [timestamp, value] 형태의 배열로 반환
            List<Object> valueArray = data.getResult().get(0).getValue();
            if (valueArray != null && valueArray.size() > 1) {
                // 두 번째 요소가 실제 값
                String valueStr = valueArray.get(1).toString();
                try {
                    this.value = Double.parseDouble(valueStr);
                } catch (NumberFormatException e) {
                    this.value = 0.0;
                }
            }
        }
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String resultType;
        private List<Result> result;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private Map<String, String> metric;
        private List<Object> value;
        private List<List<Object>> values;
    }

    // 편의 메서드
    public boolean isSuccess() {
        return "success".equals(status);
    }

    public double getValue() {
        return value;
    }

    public boolean hasValue() {
        return data != null && data.getResult() != null && !data.getResult().isEmpty();
    }
}