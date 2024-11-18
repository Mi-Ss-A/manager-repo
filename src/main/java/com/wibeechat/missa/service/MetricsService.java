//package com.wibeechat.missa.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class MetricsService {
//    private final ServerRepository serverRepository;
//    private final AlertRepository alertRepository;
//
//    public SystemMetrics getCurrentMetrics() {
//        SystemMetrics metrics = new SystemMetrics();
//
//        // 서버 상태 계산
//        List<Server> servers = serverRepository.findAll();
//        long runningServers = servers.stream()
//                .filter(s -> s.getStatus() == Server.ServerStatus.RUNNING)
//                .count();
//        metrics.setServerStatus(runningServers * 100.0 / servers.size());
//
//        // 활성 알림 수 계산
//        metrics.setAlertCount(alertRepository.countByAcknowledgedFalse());
//
//        // 시스템 부하 계산
//        double avgCpuUsage = servers.stream()
//                .mapToDouble(Server::getCpuUsage)
//                .average()
//                .orElse(0.0);
//        metrics.setSystemLoad(avgCpuUsage);
//
//        return metrics;
//    }
//}
