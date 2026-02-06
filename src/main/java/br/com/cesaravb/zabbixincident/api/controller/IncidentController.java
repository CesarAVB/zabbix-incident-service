package br.com.cesaravb.zabbixincident.api.controller;

import br.com.cesaravb.zabbixincident.application.service.IncidentService;
import br.com.cesaravb.zabbixincident.dtos.request.CreateIncidentRequest;
import br.com.cesaravb.zabbixincident.dtos.request.UpdateIncidentStatusRequest;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import br.com.cesaravb.zabbixincident.dtos.response.SuccessResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    // ====================================
    // # createIncident - Cria novo incidente
    // ====================================
    @PostMapping
    public ResponseEntity<SuccessResponse<IncidentResponse>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        IncidentResponse incident = incidentService.createIncident(request);
        SuccessResponse<IncidentResponse> response = new SuccessResponse<>(
            HttpStatus.CREATED.value(),
            "Incidente criado com sucesso",
            incident,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ====================================
    // # getAllIncidents - Lista incidentes com paginação
    // ====================================
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<IncidentResponse>>> getAllIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<IncidentResponse> incidents = incidentService.getAllIncidents(pageable);
        SuccessResponse<Page<IncidentResponse>> response = new SuccessResponse<>(
            HttpStatus.OK.value(),
            "Incidentes listados com sucesso",
            incidents,
            LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    // ====================================
    // # getIncidentById - Busca incidente por ID
    // ====================================
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<IncidentResponse>> getIncidentById(@PathVariable Long id) {
        Optional<IncidentResponse> incident = incidentService.getIncidentById(id);
        if (incident.isPresent()) {
            SuccessResponse<IncidentResponse> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Incidente encontrado",
                incident.get(),
                LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ====================================
    // # getIncidentByZabbixEventId - Busca por Zabbix Event ID
    // ====================================
    @GetMapping("/zabbix/{eventId}")
    public ResponseEntity<SuccessResponse<IncidentResponse>> getIncidentByZabbixEventId(@PathVariable String eventId) {
        Optional<IncidentResponse> incident = incidentService.getIncidentByZabbixEventId(eventId);
        if (incident.isPresent()) {
            SuccessResponse<IncidentResponse> response = new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Incidente encontrado",
                incident.get(),
                LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ====================================
    // # updateIncidentStatus - Atualiza status do incidente
    // ====================================
    @PutMapping("/{id}/status")
    public ResponseEntity<SuccessResponse<IncidentResponse>> updateIncidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request) {
        IncidentResponse incident = incidentService.updateIncidentStatus(id, request);
        SuccessResponse<IncidentResponse> response = new SuccessResponse<>(
            HttpStatus.OK.value(),
            "Status do incidente atualizado com sucesso",
            incident,
            LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    // ====================================
    // # deleteIncident - Deleta incidente
    // ====================================
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        SuccessResponse<Void> response = new SuccessResponse<>(
            HttpStatus.OK.value(),
            "Incidente deletado com sucesso",
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}