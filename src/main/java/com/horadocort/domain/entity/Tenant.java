package com.horadocort.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "owner_phone")
    private String ownerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private TenantPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status;

    @Column(name = "opening_hour", nullable = false)
    private int openingHour;

    @Column(name = "closing_hour", nullable = false)
    private int closingHour;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Column(name = "trial_ends_at")
    private ZonedDateTime trialEndsAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = ZonedDateTime.now();
        if (plan == null) plan = TenantPlan.TRIAL;
        if (status == null) status = TenantStatus.ACTIVE;
        if (timezone == null) timezone = "America/Sao_Paulo";
    }
}
