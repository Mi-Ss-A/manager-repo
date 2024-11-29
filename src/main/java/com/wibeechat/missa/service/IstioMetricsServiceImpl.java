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
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class IstioMetricsServiceImpl implements IstioMetricsService{
    private final KubernetesClient kubernetesClient;
    private final PrometheusApi prometheusApi;

    private static final String NAMESPACE = "wibee";  // 실제 wibee 네임스페이스
    /*
    wibee-ai-server-service       ClusterIP   172.20.162.241   <none>        5000/TCP   2d17h
wibee-config-server-service   ClusterIP   172.20.135.47    <none>        9000/TCP   3d19h
wibee-front-end-service       ClusterIP   172.20.251.137   <none>        80/TCP     136m
wibee-user-server-service     ClusterIP   172.20.252.43    <none>        8081/TCP   43h
     */
    private static final List<String> SERVICE_NAMES = Arrays.asList(
            "wibee-user-server-service",
            "wibee-ai-server-service",
            "wibee-config-server-service",
            "wibee-front-end-service"
    );

    // 시간대별 트래픽 데이터를 저장할 구조
    private final Map<Integer, Map<String, TrafficPattern>> hourlyTrafficPatterns = new HashMap<>();

    // 초기화 메서드
    @PostConstruct
    public void init() {
        // 24시간에 대한 데이터 구조 초기화
        for (int hour = 0; hour < 24; hour++) {
            hourlyTrafficPatterns.put(hour, new HashMap<>());
            for (String service : SERVICE_NAMES) {
                hourlyTrafficPatterns.get(hour).put(service, new TrafficPattern());
            }
        }
    }

    @Override
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();

        try {
            // 피크 시간 정보
            Map<String, String> peakHours = new HashMap<>();
            Map<String, Double> peakTraffic = new HashMap<>();
            Map<String, Integer> requiredPods = new HashMap<>();

            // 스케일링 타임라인
            Map<String, Map<String, Object>> scalingTimeline = new HashMap<>();

            // 시간별 트래픽
            Map<String, List<Double>> hourlyTraffic = new HashMap<>();

            // 각 서비스별 메트릭 수집
            for (String serviceName : SERVICE_NAMES) {
                // 피크 시간 정보 수집
                Map<String, Object> peakInfo = getPeakHourInfo(serviceName);
                peakHours.put(serviceName, (String) peakInfo.get("peakHour"));
                peakTraffic.put(serviceName, (Double) peakInfo.get("peakTraffic"));
                requiredPods.put(serviceName, calculateRequiredPods(serviceName));

                // 스케일링 타임라인 정보
                Map<String, Object> timeline = getScalingTimeline(serviceName);
                scalingTimeline.put(serviceName, timeline);

                // 24시간 트래픽 데이터
                hourlyTraffic.put(serviceName, get24HourTraffic(serviceName));
            }

            // 수집된 모든 메트릭 저장
            allMetrics.put("peakHours", peakHours);
            allMetrics.put("peakTraffic", peakTraffic);
            allMetrics.put("requiredPods", requiredPods);
            allMetrics.put("scalingTimeline", scalingTimeline);
            allMetrics.put("hourlyTraffic", hourlyTraffic);

        } catch (Exception e) {
            log.error("Failed to collect all metrics", e);
        }

        return allMetrics;
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

    private Map<String, Object> getScalingTimeline(String serviceName) {
        Map<String, Object> timeline = new HashMap<>();
        try {
            // 현재 시간 기준 타임라인 생성
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> peakInfo = getPeakHourInfo(serviceName);
            String peakHourStr = (String) peakInfo.get("peakHour");
            int peakHour = Integer.parseInt(peakHourStr.split(":")[0]);

            // 준비 상태 (피크 시간 1시간 전)
            LocalDateTime beforeTime = now.withHour(peakHour - 1);
            timeline.put("beforeTime", beforeTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeline.put("beforePods", getCurrentPodCount(serviceName));

            // 스케일링 중 (피크 시간)
            LocalDateTime duringTime = now.withHour(peakHour);
            timeline.put("duringTime", duringTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeline.put("duringPods", getTargetPodCount(serviceName));

            // 완료 상태 (피크 시간 이후)
            LocalDateTime afterTime = now.withHour(peakHour + 1);
            timeline.put("afterTime", afterTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeline.put("afterPods", getTargetPodCount(serviceName));

        } catch (Exception e) {
            log.error("Failed to get scaling timeline for service: {}", serviceName, e);
        }
        return timeline;
    }

    private List<Double> get24HourTraffic(String serviceName) {
        List<Double> hourlyData = new ArrayList<>();
        try {
            // 현재 시간 기준으로 단순 쿼리 실행
            String query = String.format(
                    "sum(rate(istio_requests_total{" +
                            "destination_workload=\"%s\"," +
                            "destination_workload_namespace=\"%s\"" +
                            "}[5m]))",
                    serviceName, NAMESPACE
            );

            log.info("Executing query for service {}: {}", serviceName, query);

            try {
                QueryResult result = prometheusApi.query(query);
                // 현재 값을 24시간 데이터로 복제 (임시 방편)
                for (int i = 0; i < 24; i++) {
                    hourlyData.add(result.getValue());
                }
            } catch (Exception e) {
                log.warn("Failed to get traffic data, using 0.0");
                // 에러시 0으로 채움
                for (int i = 0; i < 24; i++) {
                    hourlyData.add(0.0);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get 24-hour traffic data for service: {}", serviceName, e);
            // 에러 발생시 0으로 채운 24시간 데이터 반환
            hourlyData = new ArrayList<>(Collections.nCopies(24, 0.0));
        }
        return hourlyData;
    }

    private long getTimestampForHour(int hour) {
        return LocalDateTime.now()
                .withHour(hour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();
    }

    private int getCurrentPodCount(String serviceName) {
        try {
            var deployment = kubernetesClient.apps()
                    .deployments()
                    .inNamespace(NAMESPACE)
                    .withName(serviceName)
                    .get();

            return deployment != null ? deployment.getSpec().getReplicas() : 0;
        } catch (Exception e) {
            log.error("Failed to get current pod count for service: {}", serviceName, e);
            return 0;
        }
    }

    private int getTargetPodCount(String serviceName) {
        try {
            double peakTraffic = (Double) getPeakHourInfo(serviceName).get("peakTraffic");
            return calculateRequiredPods(peakTraffic);
        } catch (Exception e) {
            log.error("Failed to get target pod count for service: {}", serviceName, e);
            return 0;
        }
    }

    private int calculateRequiredPods(double traffic) {
        // 기본 설정
        double trafficPerPod = 100.0; // 각 Pod가 처리할 수 있는 초당 요청 수
        int minPods = 1;
        int maxPods = 10;

        // 필요한 Pod 수 계산
        int required = (int) Math.ceil(traffic / trafficPerPod);

        // 최소/최대 범위 내로 조정
        return Math.min(Math.max(required, minPods), maxPods);
    }

    private int calculateRequiredPods(String serviceName) {
        try {
            double currentTraffic = getCurrentTraffic(serviceName);
            return calculateRequiredPods(currentTraffic);
        } catch (Exception e) {
            log.error("Failed to calculate required pods for service: {}", serviceName, e);
            return 1;
        }
    }

    public Map<String, Object> getPeakHourInfo(String serviceName) {
        Map<String, Object> peakInfo = new HashMap<>();
        try {
            // 현재 트래픽 상태를 조회하는 쿼리
            String query = String.format(
                    "sum(rate(istio_requests_total{" +
                            "destination_workload=\"%s\"," +
                            "destination_workload_namespace=\"%s\"" +
                            "}[5m])) [24h:1h]",
                    serviceName, NAMESPACE
            );

            log.debug("Executing peak traffic query for {}: {}", serviceName, query);
            // 각 시간대별 트래픽 데이터 수집
            Map<Integer, Double> hourlyTraffic = new HashMap<>();
            for (int hour = 0; hour < 24; hour++) {
                String hourlyQuery = String.format(
                        "sum(rate(istio_requests_total{" +
                                "destination_workload=\"%s\"," +
                                "destination_workload_namespace=\"%s\"" +
                                "}[5m]))",
                        serviceName, NAMESPACE
                );

                QueryResult result = prometheusApi.query(hourlyQuery);
                hourlyTraffic.put(hour, result.getValue());
            }

            // 피크 시간 찾기
            Map.Entry<Integer, Double> peakEntry = hourlyTraffic.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(Map.entry(0, 0.0));

            int peakHour = peakEntry.getKey();
            double peakTraffic = peakEntry.getValue();

            peakInfo.put("peakHour", String.format("%02d:00", peakHour));
            peakInfo.put("peakTraffic", peakTraffic);
            peakInfo.put("hourlyTraffic", hourlyTraffic);

        } catch (Exception e) {
            log.error("Failed to get peak hour info for service: {}", serviceName, e);
            peakInfo.put("peakHour", "00:00");
            peakInfo.put("peakTraffic", 0.0);
        }

        return peakInfo;
    }

    public double getCurrentTraffic(String serviceName) throws IOException {
        String query = String.format(
                "sum(rate(istio_requests_total{destination_workload=\"%s\", destination_workload_namespace=\"%s\"}[5m]))",
                serviceName, NAMESPACE
        );

        QueryResult result = prometheusApi.query(query);
        return result.getValue();
    }


    private Map.Entry<Integer, Double> findPeakHour(String serviceName) {
        return hourlyTrafficPatterns.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        entry.getValue().get(serviceName).getAverageTraffic()))
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry(0, 0.0));
    }

    @Data
    private static class TrafficPattern {
        private double totalTraffic = 0.0;
        private int sampleCount = 0;
        private double peakTraffic = 0.0;

        public void updatePattern(double traffic) {
            totalTraffic += traffic;
            sampleCount++;
            peakTraffic = Math.max(peakTraffic, traffic);
        }

        public double getAverageTraffic() {
            return sampleCount > 0 ? totalTraffic / sampleCount : 0.0;
        }
    }

    // 각 서비스별 메트릭 수집
    private Map<String, ServiceMetrics> getServicesMetrics() throws IOException {
        Map<String, ServiceMetrics> metricsMap = new HashMap<>();

        for (String serviceName : SERVICE_NAMES) {
            double avgResponseTime = getAverageResponseTime(serviceName);
            double trafficVolume = getTrafficVolume(serviceName);

            metricsMap.put(serviceName, new ServiceMetrics(avgResponseTime, trafficVolume));

            log.info("Service: {} - Avg Response Time: {}ms, Traffic Volume: {} RPS",
                    serviceName, avgResponseTime, trafficVolume);
        }

        return metricsMap;
    }

    // 서비스별 평균 응답시간 조회
    private double getAverageResponseTime(String serviceName) throws IOException {
        String query = String.format(
                "sum(istio_request_duration_milliseconds_sum{destination_workload=\"%s\", destination_workload_namespace=\"%s\"}) / " +
                        "sum(istio_request_duration_milliseconds_count{destination_workload=\"%s\", destination_workload_namespace=\"%s\"})",
                serviceName, NAMESPACE, serviceName, NAMESPACE
        );

        QueryResult result = prometheusApi.query(query);
        return result.getValue();
    }

    // 서비스별 트래픽 볼륨 조회
    private double getTrafficVolume(String serviceName) throws IOException {
        String query = String.format(
                "sum(rate(istio_requests_total{destination_workload=\"%s\", destination_workload_namespace=\"%s\"}[5m]))",
                serviceName, NAMESPACE
        );

        QueryResult result = prometheusApi.query(query);
        return result.getValue();
    }

    // 모든 서비스 스케일링 체크
    @Scheduled(fixedRate = 300000) // 5분마다 체크
    public void checkAndScale() {
        try {
            Map<String, ServiceMetrics> metricsMap = getServicesMetrics();

            for (Map.Entry<String, ServiceMetrics> entry : metricsMap.entrySet()) {
                String serviceName = entry.getKey();
                ServiceMetrics metrics = entry.getValue();

                decideScaling(serviceName, metrics.getAvgResponseTime(), metrics.getTrafficVolume());
            }
        } catch (Exception e) {
            log.error("Failed to check Istio metrics", e);
        }
    }

    // 스케일링 결정
    private void decideScaling(String serviceName, double responseTime, double trafficVolume) {
        double responseThreshold = 500.0; // 500ms
        double trafficThreshold = 100.0;  // 100 RPS

        try {
            if (responseTime > responseThreshold || trafficVolume > trafficThreshold) {
                scaleUp(serviceName);
            } else if (responseTime < responseThreshold/2 && trafficVolume < trafficThreshold/2) {
                scaleDown(serviceName);
            } else {
                log.info("No scaling needed for service: {}. Metrics within acceptable range.", serviceName);
            }
        } catch (Exception e) {
            log.error("Failed to execute scaling decision for service: {}", serviceName, e);
        }
    }

    // 스케일 업
    private void scaleUp(String serviceName) {
        try {
            var deployment = kubernetesClient.apps()
                    .deployments()
                    .inNamespace(NAMESPACE)
                    .withName(serviceName)
                    .get();

            if (deployment != null) {
                int currentReplicas = deployment.getSpec().getReplicas();
                int maxReplicas = 10;

                if (currentReplicas < maxReplicas) {
                    int newReplicas = currentReplicas + 1;
                    kubernetesClient.apps()
                            .deployments()
                            .inNamespace(NAMESPACE)
                            .withName(serviceName)
                            .scale(newReplicas);

                    log.info("Scaled up {} from {} to {} replicas",
                            serviceName, currentReplicas, newReplicas);
                } else {
                    log.warn("Cannot scale up {}: already at maximum replicas ({})", serviceName, maxReplicas);
                }
            } else {
                log.error("Deployment {} not found in namespace {}", serviceName, NAMESPACE);
            }
        } catch (Exception e) {
            log.error("Failed to scale up deployment: {}", serviceName, e);
        }
    }

    private void scaleDown(String serviceName) {
        try {
            var deployment = kubernetesClient.apps()
                    .deployments()
                    .inNamespace(NAMESPACE)
                    .withName(serviceName)
                    .get();

            if (deployment != null) {
                int currentReplicas = deployment.getSpec().getReplicas();
                int minReplicas = 1;  // 최소 replica 수 설정

                if (currentReplicas > minReplicas) {
                    int newReplicas = currentReplicas - 1;
                    kubernetesClient.apps()
                            .deployments()
                            .inNamespace(NAMESPACE)
                            .withName(serviceName)
                            .scale(newReplicas);

                    log.info("Scaled down {} from {} to {} replicas",
                            serviceName, currentReplicas, newReplicas);
                } else {
                    log.warn("Cannot scale down {}: already at minimum replicas ({})",
                            serviceName, minReplicas);
                }
            } else {
                log.error("Deployment {} not found in namespace {}",
                        serviceName, NAMESPACE);
            }
        } catch (Exception e) {
            log.error("Failed to scale down deployment: {}", serviceName, e);
        }
    }

    @Data
    @AllArgsConstructor
    private static class ServiceMetrics {
        private double avgResponseTime;
        private double trafficVolume;
    }
}