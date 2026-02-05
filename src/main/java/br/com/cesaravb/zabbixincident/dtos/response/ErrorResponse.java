package br.com.cesaravb.zabbixincident.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO responsável por retornar erros de forma padronizada para o cliente.
 *
 * Fluxo completo de processamento:
 *
 * 1. Durante o processamento de uma requisição, ocorre um erro (validação, não encontrado, etc)
 * 2. O Controller ou GlobalExceptionHandler captura a exceção
 * 3. Constrói um ErrorResponse com detalhes do erro
 * 4. Retorna a resposta com o código HTTP apropriado (400, 404, 500, etc)
 * 5. O Spring serializa automaticamente este record em JSON
 * 6. O cliente recebe a resposta de erro com informações úteis para debug
 *
 * Tipos de erro:
 *
 * • 400 Bad Request → Validação de entrada falhou (campos faltando, formato inválido, etc)
 * • 404 Not Found → Recurso solicitado não existe
 * • 500 Internal Server Error → Erro interno do servidor
 *
 * Exemplo de resposta JSON (validação falhou):
 * {
 *   "status": 400,
 *   "message": "Validação falhou",
 *   "path": "/api/incidents",
 *   "timestamp": "2026-02-05T04:45:17",
 *   "details": {
 *     "zabbixEventId": "zabbixEventId não pode estar vazio",
 *     "title": "title não pode estar vazio",
 *     "severity": "severity não pode ser nulo"
 *   }
 * }
 *
 * Exemplo de resposta JSON (recurso não encontrado):
 * {
 *   "status": 404,
 *   "message": "Incidente não encontrado",
 *   "path": "/api/incidents/999",
 *   "timestamp": "2026-02-05T04:45:17",
 *   "details": null
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        String path,
        LocalDateTime timestamp,
        Object details
) {}