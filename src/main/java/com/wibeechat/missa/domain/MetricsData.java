package com.wibeechat.missa.domain;

import lombok.Data;

@Data
public class MetricsData {
    private long duration;
    private String page;
    private String sessionId;
}
