package br.com.cesaravb.zabbixincident.api.controller;

import br.com.cesaravb.zabbixincident.dtos.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    // ====================================
    // # checkHealth - Verifica status da aplicação
    // ====================================
    @GetMapping
    public ResponseEntity<SuccessResponse<Map<String, String>>> checkHealth() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "ZabbixIncidentService");
        healthData.put("timestamp", LocalDateTime.now().toString());
        SuccessResponse<Map<String, String>> response = new SuccessResponse<>(HttpStatus.OK.value(), "Serviço está operacional", healthData, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}