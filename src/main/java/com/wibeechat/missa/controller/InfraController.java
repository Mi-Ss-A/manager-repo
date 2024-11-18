//package com.wibeechat.missa.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class InfraController {
//    private final ServerService serverService;
//    private final AlertService alertService;
//    private final MetricsService metricsService;
//
//    @GetMapping("/dashboard")
//    public String dashboard(Model model) {
//        // 시스템 메트릭 데이터 로드
//        SystemMetrics metrics = metricsService.getCurrentMetrics();
//        model.addAttribute("serverStatus", metrics.getServerStatus());
//        model.addAttribute("alertCount", metrics.getAlertCount());
//        model.addAttribute("systemLoad", metrics.getSystemLoad());
//
//        // 활성 알림 로드
//        List<Alert> activeAlerts = alertService.getActiveAlerts();
//        model.addAttribute("alerts", activeAlerts);
//
//        // 서버 목록 로드
//        List<Server> servers = serverService.getAllServers();
//        model.addAttribute("servers", servers);
//
//        return "dashboard";
//    }
//
//    @GetMapping("/api/servers/{id}")
//    public String getServerDetails(@PathVariable Long id, Model model) {
//        Server server = serverService.getServerById(id);
//        model.addAttribute("server", server);
//        return "fragments/server-details :: serverDetailContent";
//    }
//
//    @PostMapping("/api/servers/{id}/restart")
//    @ResponseBody
//    public ResponseEntity<String> restartServer(@PathVariable Long id) {
//        serverService.restartServer(id);
//        return ResponseEntity.ok("Server restart initiated");
//    }initiate
//}