package com.wibeechat.missa.service;

import com.wibeechat.missa.domain.IstioMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public interface IstioMetricsService {
    IstioMetrics getIstioMetrics();
    Map<String, Object> getAllMetrics();
}
