package com.wibeechat.missa.service;

import com.wibeechat.missa.domain.IstioMetrics;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.istio.api.networking.v1beta1.VirtualService;
import io.fabric8.istio.api.networking.v1beta1.DestinationRule;
import org.springframework.stereotype.Service;
import io.fabric8.istio.api.networking.v1beta1.Gateway;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IstioMetricsServiceImpl implements IstioMetricsService {

    private final KubernetesClient kubernetesClient;

    public IstioMetricsServiceImpl() {
        this.kubernetesClient = new KubernetesClientBuilder().build();
    }


    @Override
    public IstioMetrics getIstioMetrics() {
        try {
            // VirtualServices 카운트
            int virtualServices = countVirtualServices();

            // DestinationRules 카운트
            int destinationRules = countDestinationRules();

            // Gateways 카운트
            int gateways = countGateways();

            // 활성 서비스 및 네임스페이스 카운트
            int activeServices = countActiveServices();
            int totalNamespaces = countNamespaces();

            // 서비스 상태 메트릭 수집
            ServiceHealthMetrics healthMetrics = collectServiceHealthMetrics();

            return IstioMetrics.builder()
                    .totalServices(activeServices)
                    .totalVirtualServices(virtualServices)
                    .totalDestinationRules(destinationRules)
                    .totalGateways(gateways)
                    .activeGateways(gateways)
                    .activeServices(activeServices)
                    .totalNamespaces(totalNamespaces)
                    .alerts(healthMetrics.getUnhealthyCount())
                    .healthyServices(healthMetrics.getHealthyCount())
                    .unhealthyServices(healthMetrics.getUnhealthyCount())
                    .averageLatency(healthMetrics.getAverageLatency())
                    .totalRequests(healthMetrics.getTotalRequests())
                    .totalErrors(healthMetrics.getTotalErrors())
                    .build();

        } catch (Exception e) {
            log.error("Failed to collect Istio metrics", e);
            return IstioMetrics.createEmpty();
        }
    }


    private int countVirtualServices() {
        try {
            return kubernetesClient.resources(VirtualService.class)
                    .inAnyNamespace()
                    .list()
                    .getItems()
                    .size();
        } catch (Exception e) {
            log.error("Failed to count virtual services", e);
            return 0;
        }
    }

    private int countDestinationRules() {
        try {
            return kubernetesClient.resources(DestinationRule.class)
                    .inAnyNamespace()
                    .list()
                    .getItems()
                    .size();
        } catch (Exception e) {
            log.error("Failed to count destination rules", e);
            return 0;
        }
    }

    private int countGateways() {
        try {
            return kubernetesClient.resources(Gateway.class)
                    .inAnyNamespace()
                    .list()
                    .getItems()
                    .size();
        } catch (Exception e) {
            log.error("Failed to count gateways", e);
            return 0;
        }
    }

    private int countActiveServices() {
        try {
            return kubernetesClient.services()
                    .inAnyNamespace()
                    .list()
                    .getItems()
                    .size();
        } catch (Exception e) {
            log.error("Failed to count active services", e);
            return 0;
        }
    }

    private int countNamespaces() {
        try {
            return kubernetesClient.namespaces()
                    .list()
                    .getItems()
                    .size();
        } catch (Exception e) {
            log.error("Failed to count namespaces", e);
            return 0;
        }
    }

    @lombok.Value
    private static class ServiceHealthMetrics {
        int healthyCount;
        int unhealthyCount;
        double averageLatency;
        int totalRequests;
        int totalErrors;
    }

    private ServiceHealthMetrics collectServiceHealthMetrics() {
        try {
            // 여기서 실제 서비스 health 메트릭을 수집하는 로직을 구현
            // 예시로 더미 데이터를 반환
            return new ServiceHealthMetrics(
                    10, // healthy services
                    2,  // unhealthy services
                    150.0, // average latency in ms
                    1000, // total requests
                    50   // total errors
            );
        } catch (Exception e) {
            log.error("Failed to collect service health metrics", e);
            return new ServiceHealthMetrics(0, 0, 0.0, 0, 0);
        }
    }
}