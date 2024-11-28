package com.wibeechat.missa.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PrometheusApi {

    private final String prometheusUrl;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public PrometheusApi(@Value("${prometheus.url}") String prometheusUrl) {
        this.prometheusUrl = prometheusUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public QueryResult query(String promQl) throws IOException {
        String encodedQuery = URLEncoder.encode(promQl, StandardCharsets.UTF_8);
        String url = String.format("%s/api/v1/query?query=%s", prometheusUrl, encodedQuery);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Unexpected response " + response);
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body().string());
            return parseResponse(jsonResponse);
        }
    }

    private QueryResult parseResponse(JsonNode jsonNode) {
        try {
            if (jsonNode.has("status") && "success".equals(jsonNode.get("status").asText())) {
                if (jsonNode.has("data") && jsonNode.get("data").has("result")) {
                    JsonNode results = jsonNode.get("data").get("result");

                    List<Double> values = new ArrayList<>();

                    if (results.isArray() && results.size() > 0) {
                        // 첫 번째 결과의 values 배열 처리
                        JsonNode firstResult = results.get(0);

                        if (firstResult.has("values")) {
                            // 여러 시점의 데이터가 있는 경우
                            JsonNode valuesArray = firstResult.get("values");
                            for (JsonNode valueNode : valuesArray) {
                                if (valueNode.isArray() && valueNode.size() >= 2) {
                                    String strValue = valueNode.get(1).asText("0");
                                    values.add(Double.parseDouble(strValue));
                                }
                            }
                        } else if (firstResult.has("value")) {
                            // 단일 시점의 데이터만 있는 경우
                            JsonNode value = firstResult.get("value");
                            if (value.isArray() && value.size() >= 2) {
                                String strValue = value.get(1).asText("0");
                                values.add(Double.parseDouble(strValue));
                            }
                        }
                    }

                    log.debug("Parsed {} values from Prometheus response", values.size());
                    return new QueryResult(values);
                }
            } else {
                String errorType = jsonNode.has("errorType") ? jsonNode.get("errorType").asText() : "unknown";
                String error = jsonNode.has("error") ? jsonNode.get("error").asText() : "unknown error";
                log.error("Prometheus query failed: {} - {}", errorType, error);
            }
        } catch (Exception e) {
            log.error("Failed to parse Prometheus response: {}", e.getMessage());
        }

        // 에러 시 기본값 반환
        return new QueryResult(Collections.singletonList(0.0));
    }

    public RangeQueryResult rangeQuery(String promQl, long startTime, long endTime, long stepSeconds) throws IOException {
        String encodedQuery = URLEncoder.encode(promQl, StandardCharsets.UTF_8);
        String url = String.format("%s/api/v1/query_range?query=%s&start=%d&end=%d&step=%d",
                prometheusUrl, encodedQuery, startTime, endTime, stepSeconds);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Unexpected response " + response);
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body().string());
            return parseRangeResponse(jsonResponse);
        }
    }

    private RangeQueryResult parseRangeResponse(JsonNode jsonNode) {
        List<RangeQueryResult.DataPoint> dataPoints = new ArrayList<>();

        try {
            if (jsonNode.has("status") && "success".equals(jsonNode.get("status").asText())) {
                if (jsonNode.has("data") && jsonNode.get("data").has("result")) {
                    JsonNode results = jsonNode.get("data").get("result");

                    if (results.isArray() && results.size() > 0) {
                        JsonNode firstResult = results.get(0);
                        if (firstResult.has("values")) {
                            JsonNode values = firstResult.get("values");
                            for (JsonNode value : values) {
                                if (value.isArray() && value.size() >= 2) {
                                    long timestamp = value.get(0).asLong();
                                    double metricValue = Double.parseDouble(value.get(1).asText("0"));
                                    dataPoints.add(new RangeQueryResult.DataPoint(timestamp, metricValue));
                                }
                            }
                        }
                    }
                }

                log.debug("Parsed {} data points from Prometheus range query response", dataPoints.size());
                return new RangeQueryResult(dataPoints);

            } else {
                String errorType = jsonNode.has("errorType") ? jsonNode.get("errorType").asText() : "unknown";
                String error = jsonNode.has("error") ? jsonNode.get("error").asText() : "unknown error";
                log.error("Prometheus range query failed: {} - {}", errorType, error);
            }
        } catch (Exception e) {
            log.error("Failed to parse Prometheus range query response: {}", e.getMessage());
        }

        // 에러 시 빈 결과 반환
        return new RangeQueryResult(Collections.emptyList());
    }
}