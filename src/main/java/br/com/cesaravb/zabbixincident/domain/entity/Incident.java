package br.com.cesaravb.zabbixincident.domain.entity;

import java.time.LocalDateTime;

import br.com.cesaravb.zabbixincident.domain.enums.IncidentStatus;
import br.com.cesaravb.zabbixincident.domain.enums.SeverityLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String zabbixEventId;

    @Column(name = "host_ids", nullable = true)
    private String hostids;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "alert_message", columnDefinition = "TEXT", nullable = true)
    private String alertMessage;

    @Column(name = "event_name", nullable = true)
    private String eventName;

    @Column(name = "event_opdata", nullable = true)
    private String eventOpdata;

    @Column(nullable = true)
    private String host;

    @Column(name = "host_ip", nullable = true)
    private String hostIp;

    @Column(nullable = true)
    private String item;

    @Column(name = "item_key", nullable = true)
    private String itemKey;

    @Column(name = "trigger_name", nullable = true)
    private String trigger;

    @Column(name = "url_zabbix", columnDefinition = "TEXT", nullable = true)
    private String urlZabbix;

    @Column(nullable = true)
    private String valor;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeverityLevel severity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    @Column(nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}