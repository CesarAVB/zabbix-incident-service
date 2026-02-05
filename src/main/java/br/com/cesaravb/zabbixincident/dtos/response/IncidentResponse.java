package br.com.cesaravb.zabbixincident.dtos.response;

import java.time.LocalDateTime;

/**
 * DTO responsável por retornar os dados de um incidente para o cliente.
 *
 * Fluxo completo de processamento:
 *
 * 1. O Service recupera um Incident do banco de dados ou cria um novo
 * 2. O Service utiliza o Mapper (MapStruct) para converter a Entity em IncidentResponse
 * 3. O Controller envolve este DTO em um SuccessResponse com metadados (status HTTP, mensagem, timestamp)
 * 4. O Spring serializa automaticamente este record em JSON
 * 5. A resposta é enviada ao cliente (Zabbix, frontend, ou sistema externo)
 *
 * Uso em diferentes cenários:
 *
 * • POST /api/incidents → Retorna HTTP 201 Created com IncidentResponse
 * • GET /api/incidents/{id} → Retorna HTTP 200 OK com IncidentResponse
 * • GET /api/incidents → Retorna HTTP 200 OK com Page<IncidentResponse>
 * • PUT /api/incidents/{id}/status → Retorna HTTP 200 OK com IncidentResponse atualizado
 * • WebSocket /topic/incidents → Envia IncidentResponse em tempo real para clientes inscritos
 *
 * Exemplo de resposta JSON:
 * {
 *   "status": 201,
 *   "message": "Incidente criado com sucesso",
 *   "data": {
 *     "id": 1,
 *     "zabbixEventId": "28316936",
 *     "hostids": "10084",
 *     "title": "ICMP Ping Down",
 *     "description": "Last three attempts returned timeout",
 *     "alertMessage": "Problem has been resolved...",
 *     "eventName": "ICMP Ping Down",
 *     "eventOpdata": "Up (1)",
 *     "host": "ENERGIA POP - SEROPEDICA",
 *     "hostIp": "10.90.92.10",
 *     "item": "ICMP ping",
 *     "itemKey": "icmpping",
 *     "trigger": "ICMP Ping Down",
 *     "urlZabbix": "https://zabbix.example.com/...",
 *     "valor": "Up (1)",
 *     "severity": "High",
 *     "status": "OPEN",
 *     "source": "zabbix",
 *     "createdAt": "2026-02-05T04:45:17",
 *     "updatedAt": "2026-02-05T04:45:17"
 *   },
 *   "timestamp": "2026-02-05T04:45:17"
 * }
 */
public record IncidentResponse(
        Long id,
        String zabbixEventId,
        String hostids,
        String title,
        String description,
        String alertMessage,
        String eventName,
        String eventOpdata,
        String host,
        String hostIp,
        String item,
        String itemKey,
        String trigger,
        String urlZabbix,
        String valor,
        String severity,
        String status,
        String source,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}