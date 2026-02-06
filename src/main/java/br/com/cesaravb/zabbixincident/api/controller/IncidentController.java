package br.com.cesaravb.zabbixincident.api.controller;

import br.com.cesaravb.zabbixincident.application.service.IncidentService;
import br.com.cesaravb.zabbixincident.dtos.request.CreateIncidentRequest;
import br.com.cesaravb.zabbixincident.dtos.request.UpdateIncidentStatusRequest;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import br.com.cesaravb.zabbixincident.dtos.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Endpoints:
 * • POST /api/incidents - Criar novo incidente
 * • GET /api/incidents - Listar todos (paginado)
 * • GET /api/incidents/{id} - Buscar por ID
 * • GET /api/incidents/zabbix/{zabbixEventId} - Buscar por Zabbix Event ID
 * • GET /api/incidents/host/{hostids} - Buscar por Host ID
 * • PUT /api/incidents/{id}/status - Atualizar status
 * • DELETE /api/incidents/{id} - Deletar incidente
 */
@Slf4j
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    // ====================================
    // # POST /api/incidents - Criar novo incidente
    // ====================================
    @PostMapping
    public ResponseEntity<SuccessResponse<IncidentResponse>> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        log.info("=".repeat(80));
        log.info("🌐 [HTTP POST] /api/incidents");
        log.info("   - Content-Type: application/json");
        log.info("   - Remote Address: {}", getRemoteAddress());
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        // ====================================
        // # Validação automática (@Valid)
        // ====================================
        log.info("✅ [VALIDATION] @Valid passou na validação");
        log.info("   - zabbixEventId: {}", request.zabbixEventId());
        log.info("   - title: {}", request.title());
        log.info("   - severity: {}", request.severity());
        log.info("   - source: {}", request.source());
        
        // ====================================
        // # Chamar Service
        // ====================================
        log.info("📌 [BUSINESS LOGIC] Chamando IncidentService.createIncident()");
        IncidentResponse response = incidentService.createIncident(request);
        log.info("✅ [BUSINESS LOGIC] Service retornou com sucesso");
        log.info("   - Incidente ID: {}", response.id());
        
        // ====================================
        // # Montar Response
        // ====================================
        log.info("📦 [RESPONSE] Montando SuccessResponse");
        SuccessResponse<IncidentResponse> successResponse = new SuccessResponse<>(201, "Incidente criado com sucesso", response, LocalDateTime.now());
        log.info("✅ [RESPONSE] Response montado");
        
        // ====================================
        // # Retornar HTTP 201 Created
        // ====================================
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 201 Created");
        log.info("   - Body: SuccessResponse<IncidentResponse>");
        log.info("   - Location: /api/incidents/{}", response.id());
        log.info("=".repeat(80));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }

    // ====================================
    // # GET /api/incidents - Listar todos
    // ====================================
    public ResponseEntity<SuccessResponse<Page<IncidentResponse>>> getAllIncidents(Pageable pageable) {
        log.info("🌐 [HTTP GET] /api/incidents");
        log.info("   - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        log.info("📌 [BUSINESS LOGIC] Chamando IncidentService.getAllIncidents()");
        Page<IncidentResponse> response = incidentService.getAllIncidents(pageable);
        log.info("✅ [BUSINESS LOGIC] Service retornou com sucesso");
        log.info("   - Total: {}, Atual: {}", response.getTotalElements(), response.getNumberOfElements());
        
        SuccessResponse<Page<IncidentResponse>> successResponse = new SuccessResponse<>(200, "Incidentes listados com sucesso", response, LocalDateTime.now());
        
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 200 OK");
        log.info("=".repeat(80));
        
        return ResponseEntity.ok(successResponse);
    }

    // ====================================
    // # GET /api/incidents/{id} - Buscar por ID
    // ====================================
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<IncidentResponse>> getIncidentById(@PathVariable Long id) {
        log.info("🌐 [HTTP GET] /api/incidents/{}", id);
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        log.info("📌 [BUSINESS LOGIC] Buscando incidente com ID: {}", id);
        IncidentResponse response = incidentService.getIncidentById(id);
        log.info("✅ [BUSINESS LOGIC] Incidente encontrado: {}", response.title());
        
        SuccessResponse<IncidentResponse> successResponse = new SuccessResponse<>(200, "Incidente encontrado", response, LocalDateTime.now());
        
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 200 OK");
        log.info("=".repeat(80));
        
        return ResponseEntity.ok(successResponse);
    }

    // ====================================
    // # GET /api/incidents/zabbix/{zabbixEventId} - Buscar por Zabbix Event ID
    // ====================================
    @GetMapping("/zabbix/{zabbixEventId}")
    public ResponseEntity<SuccessResponse<IncidentResponse>> getIncidentByZabbixEventId(@PathVariable String zabbixEventId) {
        log.info("🌐 [HTTP GET] /api/incidents/zabbix/{}", zabbixEventId);
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        log.info("📌 [BUSINESS LOGIC] Buscando incidente com Zabbix Event ID: {}", zabbixEventId);
        IncidentResponse response = incidentService.getIncidentByZabbixEventId(zabbixEventId);
        log.info("✅ [BUSINESS LOGIC] Incidente encontrado: {}", response.title());
        
        SuccessResponse<IncidentResponse> successResponse = new SuccessResponse<>(200, "Incidente encontrado", response, LocalDateTime.now());
        
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 200 OK");
        log.info("=".repeat(80));
        
        return ResponseEntity.ok(successResponse);
    }

    // ====================================
    // # GET /api/incidents/host/{hostids} - Buscar por Host ID
    // ====================================
    @GetMapping("/host/{hostids}")
    public ResponseEntity<SuccessResponse<Page<IncidentResponse>>> getIncidentByHostids(@PathVariable String hostids, Pageable pageable) {
        log.info("🌐 [HTTP GET] /api/incidents/host/{}", hostids);
        log.info("   - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        log.info("📌 [BUSINESS LOGIC] Buscando incidentes do host: {}", hostids);
        Page<IncidentResponse> response = incidentService.getIncidentByHostids(hostids, pageable);
        log.info("✅ [BUSINESS LOGIC] Encontrados {} incidentes", response.getTotalElements());
        
        SuccessResponse<Page<IncidentResponse>> successResponse = new SuccessResponse<>(200, "Incidentes do host encontrados", response, LocalDateTime.now());
        
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 200 OK");
        log.info("=".repeat(80));
        
        return ResponseEntity.ok(successResponse);
    }

    // ====================================
    // # PUT /api/incidents/{id}/status - Atualizar status
    // ====================================
    @PutMapping("/{id}/status")
    public ResponseEntity<SuccessResponse<IncidentResponse>> updateIncidentStatus(@PathVariable Long id, @Valid @RequestBody UpdateIncidentStatusRequest request) {
        log.info("=".repeat(80));
        log.info("🌐 [HTTP PUT] /api/incidents/{}/status", id);
        log.info("   - Content-Type: application/json");
        log.info("   - Novo Status: {}", request.status());
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        log.info("✅ [VALIDATION] @Valid passou na validação");
        
        log.info("📌 [BUSINESS LOGIC] Atualizando status do incidente");
        IncidentResponse response = incidentService.updateIncidentStatus(id, request.status());
        log.info("✅ [BUSINESS LOGIC] Status atualizado com sucesso");
        log.info("   - ID: {}, Novo Status: {}", response.id(), response.status());
        
        SuccessResponse<IncidentResponse> successResponse = new SuccessResponse<>(200, "Status do incidente atualizado com sucesso", response, LocalDateTime.now());
        
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 200 OK");
        log.info("=".repeat(80));
        
        return ResponseEntity.ok(successResponse);
    }

    // ====================================
    // # DELETE /api/incidents/{id} - Deletar
    // ====================================
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteIncident(@PathVariable Long id) {
        log.info("🌐 [HTTP DELETE] /api/incidents/{}", id);
        log.info("   - Timestamp: {}", LocalDateTime.now());
        
        log.info("📌 [BUSINESS LOGIC] Deletando incidente com ID: {}", id);
        incidentService.deleteIncident(id);
        log.info("✅ [BUSINESS LOGIC] Incidente deletado com sucesso");
        
        SuccessResponse<Void> successResponse = new SuccessResponse<>(204, "Incidente deletado com sucesso", null, LocalDateTime.now());
        
        log.info("📤 [HTTP RESPONSE] Retornando HTTP 204 No Content");
        log.info("=".repeat(80));
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(successResponse);
    }

    // ====================================
    // # Método auxiliar para obter Remote Address
    // ====================================
    private String getRemoteAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}