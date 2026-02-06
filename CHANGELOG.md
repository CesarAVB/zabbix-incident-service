## [1.3.0](https://github.com/CesarAVB/zabbix-incident-service/compare/v1.2.0...v1.3.0) (2026-02-06)

### Features

* implementa IncidentController com endpoints para gerenciamento de ([8727a24](https://github.com/CesarAVB/zabbix-incident-service/commit/8727a24ea7110ca633b7f5827ba9cc80b3c3ebd6))

## [1.2.0](https://github.com/CesarAVB/zabbix-incident-service/compare/v1.1.0...v1.2.0) (2026-02-06)

### Features

* adiciona dependências do Lombok e MapStruct no pom.xml ([02bcdcb](https://github.com/CesarAVB/zabbix-incident-service/commit/02bcdcb93d27106d0cc99c9aa44e522120307564))
* adiciona enums IncidentStatus e SeverityLevel para gerenciamento ([3d03145](https://github.com/CesarAVB/zabbix-incident-service/commit/3d03145e2c5b4a7849d9babb797f368ba7b304f8))
* adiciona JavaDocs detalhadas ao IncidentMapper para conversão de ([5312b32](https://github.com/CesarAVB/zabbix-incident-service/commit/5312b32a90faa493391286c9b9d656af672adff7))
* atualiza a entidade Incident para incluir enums IncidentStatus e ([6b8c0f3](https://github.com/CesarAVB/zabbix-incident-service/commit/6b8c0f333986ae24ef816c8c8f0b7b2edbef3b7f))
* atualiza CHANGELOG.md para incluir novas funcionalidades e ([e584f28](https://github.com/CesarAVB/zabbix-incident-service/commit/e584f285c0410f6d059d09b918ccf67d4a112bd3))
* atualiza configurações de conexão e adiciona suporte a WebSocket e ([137c8a5](https://github.com/CesarAVB/zabbix-incident-service/commit/137c8a5c4a6cdd42745aef3b529d3042cc49a9d9))
* refatora o uso de IncidentStatus na classe IncidentService ([eb54413](https://github.com/CesarAVB/zabbix-incident-service/commit/eb54413be4c5210eb44696b0d8217d3454fbdd87))
* Torna configurações de CORS e WebSocket parametrizáveis por ([652d09c](https://github.com/CesarAVB/zabbix-incident-service/commit/652d09c8da4117a9cd55be00ad09eaa29ebfa14c))

## [1.1.0](https://github.com/CesarAVB/zabbix-incident-service/compare/v1.0.0...v1.1.0) (2026-02-05)

### Features

* adiciona JavaDocs detalhadas aos DTOs de request e response do ([f115cbf](https://github.com/CesarAVB/zabbix-incident-service/commit/f115cbfb9c869b3c09ce507c10bd7a9e2937fcb4))
* adiciona novos campos à entidade Incident para detalhamento de ([2eddbbb](https://github.com/CesarAVB/zabbix-incident-service/commit/2eddbbbb66024427f32f389a0ab31834ecbf737c))

## 1.0.0 (2026-02-05)

### Features

* adiciona configurações de ambiente para produção e desenvolvimento ([cf52871](https://github.com/CesarAVB/zabbix-incident-service/commit/cf52871e534dc2ddcc217bcfcebd8f97678e3368))
* adiciona configurações de CORS, RabbitMQ e WebSocket para ([2d31911](https://github.com/CesarAVB/zabbix-incident-service/commit/2d3191147f4ba93935690680a3fdaed469da8587))
* adiciona dependências e renomeia teste de inicialização do projeto ([e402a6b](https://github.com/CesarAVB/zabbix-incident-service/commit/e402a6bb4a3d5c53217325672a00199f7afdeaa3))
* adiciona DTOs para criação e atualização de incidentes, além de ([3993159](https://github.com/CesarAVB/zabbix-incident-service/commit/3993159bc419bdeaa2552e63ee987527a1087589))
* adiciona entidade Incident com atributos e enums para ([03b5429](https://github.com/CesarAVB/zabbix-incident-service/commit/03b5429ca20de687340f049a72a239efe81a5a57))
* adiciona GlobalExceptionHandler para tratamento de exceções na API ([de7508f](https://github.com/CesarAVB/zabbix-incident-service/commit/de7508feab9ebf916c9c93cada9a5e86903f0d7f))
* adiciona HealthController para verificação de status da aplicação ([85579bb](https://github.com/CesarAVB/zabbix-incident-service/commit/85579bb29b122a8817ab68816a5dc0e1e09aeb38))
* adiciona IncidentListener para processar mensagens de incidentes ([baacb58](https://github.com/CesarAVB/zabbix-incident-service/commit/baacb5835ac6d8c25b5bc47d1eea0485c505d182))
* adiciona IncidentMapper para conversão entre CreateIncidentRequest ([fb70803](https://github.com/CesarAVB/zabbix-incident-service/commit/fb708032e271e27bf1b72a871629e63588915da5))
* adiciona IncidentRepository para gerenciamento de incidentes no ([c2dea02](https://github.com/CesarAVB/zabbix-incident-service/commit/c2dea02ef13116751975140f494c73db9108af82))
* adiciona IncidentService e WebSocketNotificationService para ([0c318f9](https://github.com/CesarAVB/zabbix-incident-service/commit/0c318f98be9f2f960caba03c19f36f47b135140a))
* substitui ZabbixIncidentServiceApplication por Startup como ponto ([ccad2f6](https://github.com/CesarAVB/zabbix-incident-service/commit/ccad2f62b9194142b93c528fa50921472bc2ba5b))
