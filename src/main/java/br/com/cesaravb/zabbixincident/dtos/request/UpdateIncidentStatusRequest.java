package br.com.cesaravb.zabbixincident.dtos.request;

import jakarta.validation.constraints.NotNull;

public record UpdateIncidentStatusRequest(
        @NotNull(message = "status não pode ser nulo")
        String status
) {}