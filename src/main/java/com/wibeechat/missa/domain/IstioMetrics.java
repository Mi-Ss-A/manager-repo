package com.wibeechat.missa.domain;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IstioMetrics {
    private int totalServices;
    private int totalVirtualServices;
    private int totalDestinationRules;
    private int totalGateways;
    private int activeGateways;
    private int activeServices;
    private int totalNamespaces;
    private int alerts;
    private int healthyServices;
    private int unhealthyServices;
    private double averageLatency;
    private int totalRequests;
    private int totalErrors;
    private int minPods;
    private int maxPods;
    private int currentPods;
    private int cpuThreshold;
    private int memoryThreshold;

    public static IstioMetrics createEmpty() {
        return IstioMetrics.builder()
                .totalServices(0)
                .totalVirtualServices(0)
                .totalDestinationRules(0)
                .totalGateways(0)
                .activeGateways(0)
                .activeServices(0)
                .totalNamespaces(0)
                .alerts(0)
                .healthyServices(0)
                .unhealthyServices(0)
                .averageLatency(0.0)
                .totalRequests(0)
                .totalErrors(0)
                .minPods(0)
                .maxPods(0)
                .currentPods(0)
                .cpuThreshold(0)
                .memoryThreshold(0)
                .build();
    }
}