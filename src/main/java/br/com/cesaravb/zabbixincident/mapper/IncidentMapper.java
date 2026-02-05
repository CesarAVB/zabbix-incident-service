package br.com.cesaravb.zabbixincident.mapper;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import br.com.cesaravb.zabbixincident.dtos.request.CreateIncidentRequest;
import br.com.cesaravb.zabbixincident.dtos.response.IncidentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    // ====================================
    // # toEntity - Converte CreateIncidentRequest para Incident
    // ====================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "severity", target = "severity")
    Incident toEntity(CreateIncidentRequest request);

    // ====================================
    // # toResponse - Converte Incident para IncidentResponse
    // ====================================
    @Mapping(source = "severity", target = "severity")
    @Mapping(source = "status", target = "status")
    IncidentResponse toResponse(Incident incident);
}