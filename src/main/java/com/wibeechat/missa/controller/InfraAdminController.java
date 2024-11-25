package com.wibeechat.missa.controller;

import com.wibeechat.missa.domain.IstioMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/infra")
public class InfraAdminController {
    private final IstioMetricsService istioMetricsService;

    // 메인 대시보드 페이지
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            IstioMetrics metrics = istioMetricsService.getIstioMetrics();
            model.addAttribute("metrics", metrics);
            model.addAttribute("error", null);
        } catch (Exception e) {
            log.error("Failed to fetch Istio metric  s", e);
            model.addAttribute("metrics", new IstioMetrics());
            model.addAttribute("error", "Failed to fetch Istio metrics: " + e.getMessage());
        }
        return "dashboard";
    }
}
