package br.com.cesaravb.zabbixincident.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        String path,
        LocalDateTime timestamp,
        Object details
) {}