package br.com.cesaravb.zabbixincident.domain.repository;

import br.com.cesaravb.zabbixincident.domain.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // ====================================
    // # findByZabbixEventId - Buscar por Zabbix Event ID
    // ====================================
    Optional<Incident> findByZabbixEventId(String zabbixEventId);

    // ====================================
    // # findByHostids - Buscar por Host ID com paginação
    // ====================================
    Page<Incident> findByHostids(String hostids, Pageable pageable);

    // ====================================
    // # findByStatus - Buscar por Status com paginação
    // ====================================
    Page<Incident> findByStatus(String status, Pageable pageable);

    // ====================================
    // # findByCreatedAtBetween - Buscar por range de datas
    // ====================================
    Page<Incident> findByCreatedAtBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // ====================================
    // # countByStatus - Contar por Status
    // ====================================
    long countByStatus(String status);

    // ====================================
    // # findAllUnresolved - Buscar não resolvidos (JPQL)
    // ====================================
    @Query("SELECT i FROM Incident i WHERE i.status IN ('OPEN', 'IN_PROGRESS') ORDER BY i.createdAt DESC")
    Page<Incident> findAllUnresolved(Pageable pageable);

    // ====================================
    // # findBySeverity - Buscar por Severidade (JPQL)
    // ====================================
    @Query("SELECT i FROM Incident i WHERE i.severity = :severity ORDER BY i.createdAt DESC")
    Page<Incident> findBySeverity(@Param("severity") String severity, Pageable pageable);

    // ====================================
    // # findByTitleContainingIgnoreCase - Buscar por título (contains)
    // ====================================
    Page<Incident> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // ====================================
    // # findByHostContainingIgnoreCase - Buscar por nome do host
    // ====================================
    Page<Incident> findByHostContainingIgnoreCase(String host, Pageable pageable);

    // ====================================
    // # findRecentCritical - Query SQL Nativa (Advanced)
    // ====================================
    @Query(
            value = "SELECT * FROM incidents " +
                    "WHERE severity = 'CRITICAL' " +
                    "AND status IN ('OPEN', 'IN_PROGRESS') " +
                    "ORDER BY created_at DESC " +
                    "LIMIT 10",
            nativeQuery = true
    )
    List<Incident> findRecentCritical();

    // ====================================
    // # findIncidentStatistics - Query SQL Nativa (Relatório)
    // ====================================
    @Query(
            value = "SELECT status, COUNT(*) as count " +
                    "FROM incidents " +
                    "GROUP BY status " +
                    "ORDER BY count DESC",
            nativeQuery = true
    )
    List<Object[]> findIncidentStatistics();

    // ====================================
    // # findByZabbixEventIdContaining - Busca parcial de Zabbix Event ID
    // ====================================
    List<Incident> findByZabbixEventIdContaining(String zabbixEventId);

    // ====================================
    // # Notas sobre Performance
    // ====================================
    /*
     * ÍNDICES RECOMENDADOS:
     * 
     * CREATE INDEX idx_zabbix_event_id ON incidents(zabbix_event_id);
     * CREATE INDEX idx_host_ids ON incidents(host_ids);
     * CREATE INDEX idx_status ON incidents(status);
     * CREATE INDEX idx_severity ON incidents(severity);
     * CREATE INDEX idx_created_at ON incidents(created_at DESC);
     * CREATE INDEX idx_host ON incidents(host);
     * CREATE FULLTEXT INDEX idx_title ON incidents(title);
     * 
     * Estas queries serão MUITO mais rápidas com índices!
     *
     * PAGINAÇÃO:
     * 
     * Sempre use Pageable para queries que retornam muitos resultados.
     * Nunca carregue tudo na memória com findAll() sem limite.
     * 
     * Exemplo:
     * // ❌ Ruim - carrega TUDO
     * List<Incident> all = repository.findAll();
     * 
     * // ✅ Bom - pagina
     * Page<Incident> page = repository.findAll(PageRequest.of(0, 100));
     *
     * LAZY LOADING:
     * 
     * Por padrão, relacionamentos são LAZY (carregados sob demanda).
     * Se precisar de dados relacionados, use @Query com JOIN FETCH.
     *
     * TRANSAÇÃO:
     * 
     * Spring JPA aplica @Transactional automaticamente em métodos públicos.
     * Qualquer save/update/delete é persistido ao fim do método.
     */
}