package br.com.cesaravb.zabbixincident.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateIncidentRequest(
        @NotBlank(message = "zabbixEventId não pode estar vazio")
        String zabbixEventId,

        @NotBlank(message = "title não pode estar vazio")
        String title,

        String description,

        @NotNull(message = "severity não pode ser nulo")
        String severity,

        @NotBlank(message = "source não pode estar vazio")
        String source
) {}