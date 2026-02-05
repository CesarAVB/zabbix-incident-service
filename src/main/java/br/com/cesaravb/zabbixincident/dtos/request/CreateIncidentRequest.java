package br.com.cesaravb.zabbixincident.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO responsável por receber os dados de um novo incidente do Zabbix.
 *
 * Fluxo completo de processamento:
 *
 * 1. O Zabbix envia um alerta via HTTP POST para o endpoint /api/incidents
 * 2. O Controller recebe a requisição e faz o bind automático do JSON para este DTO
 * 3. O Spring executa as validações declaradas com @Valid, @NotNull e @NotBlank
 * 4. Se houver erro de validação, retorna HTTP 400 Bad Request com detalhes dos campos inválidos
 * 5. Se validação passar, o Service converte este DTO em uma Entity utilizando MapStruct
 * 6. A Entity é persistida no banco de dados (INSERT no MySQL)
 * 7. Após persistência, o incidente é publicado em uma fila RabbitMQ para processamento assíncrono
 * 8. O Listener consome a mensagem da fila e notifica o frontend via WebSocket em tempo real
 *
 * Exemplo de requisição:
 * POST /api/incidents
 * Content-Type: application/json
 *
 * {
 *   "zabbixEventId": "28316936",
 *   "hostids": "10084",
 *   "title": "ICMP Ping Down",
 *   "description": "Last three attempts returned timeout",
 *   "alertMessage": "Problem has been resolved...",
 *   "eventName": "ICMP Ping Down",
 *   "eventOpdata": "Up (1)",
 *   "host": "ENERGIA POP - SEROPEDICA",
 *   "hostIp": "10.90.92.10",
 *   "item": "ICMP ping",
 *   "itemKey": "icmpping",
 *   "trigger": "ICMP Ping Down",
 *   "urlZabbix": "https://zabbix.example.com/...",
 *   "valor": "Up (1)",
 *   "severity": "High",
 *   "source": "zabbix"
 * }
 */
public record CreateIncidentRequest(
		
        @NotBlank(message = "zabbixEventId não pode estar vazio")
        String zabbixEventId,

        String hostids,

        @NotBlank(message = "title não pode estar vazio")
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

        @NotNull(message = "severity não pode ser nulo")
        String severity,

        @NotBlank(message = "source não pode estar vazio")
        String source
) {}