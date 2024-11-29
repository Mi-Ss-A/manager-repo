package com.wibeechat.missa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wibeechat.missa.domain.IstioMetrics;
import com.wibeechat.missa.domain.PrometheusResponse;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.istio.api.networking.v1beta1.VirtualService;
import io.fabric8.istio.api.networking.v1beta1.DestinationRule;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.fabric8.istio.api.networking.v1beta1.Gateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            // 1. 전체 서비스 리스트 조회
            List<io.fabric8.kubernetes.api.model.Service> services = kubernetesClient.services()
                    .inAnyNamespace()
                    .list()
                    .getItems();

            // 2. 서비스 상태 확인
            int healthyCount = 0;
            int unhealthyCount = 0;
            int totalRequests = 0;
            int totalErrors = 0;
            double totalLatency = 0.0;

            for (io.fabric8.kubernetes.api.model.Service service : services) {
                try {
                    // 서비스의 엔드포인트 조회
                    Endpoints endpoints = kubernetesClient.endpoints()
                            .inNamespace(service.getMetadata().getNamespace())
                            .withName(service.getMetadata().getName())
                            .get();

                    // 엔드포인트가 있고 Ready 상태인 경우 healthy로 판단
                    if (endpoints != null && !endpoints.getSubsets().isEmpty() &&
                            endpoints.getSubsets().stream()
                                    .anyMatch(subset -> subset.getAddresses() != null &&
                                            !subset.getAddresses().isEmpty())) {
                        healthyCount++;

                        // 서비스 메트릭 수집 (Istio Prometheus에서)
                        Map<String, String> labels = new HashMap<>();
                        labels.put("destination_service",
                                service.getMetadata().getName() + "." +
                                        service.getMetadata().getNamespace() + ".svc.cluster.local");

                        // 요청 수, 에러 수, 레이턴시 수집
                        PrometheusResponse requests = queryPrometheus(
                                "sum(istio_requests_total{" + formatLabels(labels) + "})"
                        );
                        PrometheusResponse errors = queryPrometheus(
                                "sum(istio_requests_total{" + formatLabels(labels) +
                                        ",response_code=~\"[45].*\"})"
                        );
                        PrometheusResponse latency = queryPrometheus(
                                "histogram_quantile(0.95, sum(rate(istio_request_duration_milliseconds_bucket{" +
                                        formatLabels(labels) + "}[5m])) by (le))"
                        );

                        if (requests != null) {
                            totalRequests += requests.getValue();
                        }
                        if (errors != null) {
                            totalErrors += errors.getValue();
                        }
                        if (latency != null) {
                            totalLatency += latency.getValue();
                        }

                    } else {
                        unhealthyCount++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to check health for service: {}",
                            service.getMetadata().getName(), e);
                    unhealthyCount++;
                }
            }

            // 평균 레이턴시 계산
            double averageLatency = healthyCount > 0 ? totalLatency / healthyCount : 0.0;

            return new ServiceHealthMetrics(
                    healthyCount,
                    unhealthyCount,
                    averageLatency,
                    totalRequests,
                    totalErrors
            );

        } catch (Exception e) {
            log.error("Failed to collect service health metrics", e);
            return new ServiceHealthMetrics(0, 0, 0.0, 0, 0);
        }
    }

    // Prometheus 쿼리 헬퍼 메소드
    private PrometheusResponse queryPrometheus(String query) {
        try {
            // Prometheus API 엔드포인트 설정
            String prometheusUrl = "http://prometheus.wibeechat.com";
            RestTemplate restTemplate = new RestTemplate();

            // ResponseEntity를 PrometheusResponse로 직접 매핑
            ResponseEntity<PrometheusResponse> response = restTemplate.getForEntity(
                    prometheusUrl + "/api/v1/query?query=" + URLEncoder.encode(query, "UTF-8"),
                    PrometheusResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.warn("Failed to query Prometheus: {}", query, e);
            return null;
        }
    }

    // 레이블 포맷팅 헬퍼 메소드
    private String formatLabels(Map<String, String> labels) {
        return labels.entrySet().stream()
                .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
                .collect(Collectors.joining(","));
    }
}