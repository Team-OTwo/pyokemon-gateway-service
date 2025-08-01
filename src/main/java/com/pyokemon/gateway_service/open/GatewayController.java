package com.pyokemon.gateway_service.open;

import com.pyokemon.gateway_service.common.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/gateway/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class GatewayController {
    
    @GetMapping(value = "/hello")
    public ResponseDto<String> hello() {
        log.info("Gateway hello endpoint called");
        return ResponseDto.success("안녕!");
    }
    
    @GetMapping(value = "/health")
    public ResponseDto<Map<String, Object>> health() {
        log.info("Gateway health check requested");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "gateway-service");
        healthInfo.put("version", "1.0.0");
        
        return ResponseDto.success(healthInfo);
    }
    
    @GetMapping(value = "/status")
    public ResponseDto<Map<String, Object>> status() {
        log.info("Gateway status check requested");
        
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("service", "gateway-service");
        statusInfo.put("port", 8086);
        statusInfo.put("uptime", System.currentTimeMillis());
        statusInfo.put("active", true);
        
        return ResponseDto.success(statusInfo);
    }
}
