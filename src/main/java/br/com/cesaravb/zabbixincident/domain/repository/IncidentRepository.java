package br.com.cesaravb.zabbixincident.domain.repository;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByZabbixEventId(String zabbixEventId);
}