package com.wibeechat.missa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationRule {
    private String apiVersion = "networking.istio.io/v1alpha3";
    private String kind = "DestinationRule";
    private ObjectMeta metadata;
    private DestinationRuleSpec spec;

    @Data
    public static class DestinationRuleSpec {
        private String host;
        private TrafficPolicy trafficPolicy;
        private List<Subset> subsets;
    }

    @Data
    public static class TrafficPolicy {
        private LoadBalancer loadBalancer;
        private ConnectionPool connectionPool;
        private OutlierDetection outlierDetection;
        private TLS tls;
    }

    @Data
    public static class LoadBalancer {
        private String simple;
        private ConsistentHash consistentHash;
    }

    @Data
    public static class ConsistentHash {
        private String httpHeaderName;
        private Integer minimumRingSize;
    }

    @Data
    public static class ConnectionPool {
        private TCP tcp;
        private HTTP http;
    }

    @Data
    public static class TCP {
        private Integer maxConnections;
        private Integer connectTimeout;
    }

    @Data
    public static class HTTP {
        private Integer http1MaxPendingRequests;
        private Integer http2MaxRequests;
        private Integer maxRequestsPerConnection;
        private Integer maxRetries;
    }

    @Data
    public static class OutlierDetection {
        private Integer consecutive5xxErrors;
        private String interval;
        private String baseEjectionTime;
        private Integer maxEjectionPercent;
    }

    @Data
    public static class TLS {
        private String mode;
        private String clientCertificate;
        private String privateKey;
        private String caCertificates;
    }

    @Data
    public static class Subset {
        private String name;
        private Map<String, String> labels;
        private TrafficPolicy trafficPolicy;
    }
}