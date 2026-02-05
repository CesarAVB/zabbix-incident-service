package br.com.cesaravb.zabbixincident.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse<T>(
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {}