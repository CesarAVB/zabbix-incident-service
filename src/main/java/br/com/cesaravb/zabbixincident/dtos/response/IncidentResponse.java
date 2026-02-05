package br.com.cesaravb.zabbixincident.dtos.response;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
        String zabbixEventId,
        String title,
        String description,
        String severity,
        String status,
        String source,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}