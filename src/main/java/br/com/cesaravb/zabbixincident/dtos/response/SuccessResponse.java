package br.com.cesaravb.zabbixincident.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO genérico responsável por envolver qualquer resposta bem-sucedida de forma padronizada.
 *
 * Fluxo completo de processamento:
 *
 * 1. O Service processa a requisição e retorna um resultado (entidade, lista, etc)
 * 2. O Controller envolve o resultado em um SuccessResponse com metadados
 * 3. O SuccessResponse contém: código HTTP, mensagem em português, dados, timestamp
 * 4. O Spring serializa automaticamente este record genérico em JSON
 * 5. O cliente recebe uma resposta estruturada e padronizada
 *
 * Padrão de resposta para diferentes operações:
 *
 * POST (Criar):
 * {
 *   "status": 201,
 *   "message": "Incidente criado com sucesso",
 *   "data": { IncidentResponse },
 *   "timestamp": "2026-02-05T04:45:17"
 * }
 *
 * GET (Buscar um):
 * {
 *   "status": 200,
 *   "message": "Incidente encontrado",
 *   "data": { IncidentResponse },
 *   "timestamp": "2026-02-05T04:45:17"
 * }
 *
 * GET (Listar todos):
 * {
 *   "status": 200,
 *   "message": "Incidentes listados com sucesso",
 *   "data": { Page<IncidentResponse> },
 *   "timestamp": "2026-02-05T04:45:17"
 * }
 *
 * PUT (Atualizar):
 * {
 *   "status": 200,
 *   "message": "Status do incidente atualizado com sucesso",
 *   "data": { IncidentResponse },
 *   "timestamp": "2026-02-05T04:45:17"
 * }
 *
 * DELETE (Deletar):
 * {
 *   "status": 204,
 *   "message": "Incidente deletado com sucesso",
 *   "data": null,
 *   "timestamp": "2026-02-05T04:45:17"
 * }
 *
 * Genérico: Permite envolver qualquer tipo de dado (T) de forma type-safe
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse<T>(
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {}