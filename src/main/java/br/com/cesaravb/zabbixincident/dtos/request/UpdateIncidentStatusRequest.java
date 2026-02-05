package br.com.cesaravb.zabbixincident.dtos.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO responsável por atualizar o status de um incidente existente.
 *
 * Fluxo completo de processamento:
 *
 * 1. O frontend ou sistema externo envia um PUT para /api/incidents/{id}/status
 * 2. O Controller recebe a requisição e faz o bind automático do JSON para este DTO
 * 3. O Spring executa as validações declaradas com @Valid e @NotNull
 * 4. Se houver erro de validação, retorna HTTP 400 Bad Request com detalhes
 * 5. Se validação passar, o Service busca o incidente pelo ID no banco de dados
 * 6. Se não encontrar, retorna HTTP 404 Not Found
 * 7. Se encontrar, atualiza o status do incidente
 * 8. O incidente atualizado é salvo no banco de dados (UPDATE no MySQL)
 * 9. Após atualização, o incidente é publicado na fila RabbitMQ
 * 10. O Listener consome e notifica o frontend via WebSocket sobre a alteração
 *
 * Status válidos: OPEN, IN_PROGRESS, RESOLVED, CLOSED
 *
 * Exemplo de requisição:
 * PUT /api/incidents/1/status
 * Content-Type: application/json
 *
 * {
 *   "status": "RESOLVED"
 * }
 */
public record UpdateIncidentStatusRequest(
        @NotNull(message = "status não pode ser nulo")
        String status
) {}