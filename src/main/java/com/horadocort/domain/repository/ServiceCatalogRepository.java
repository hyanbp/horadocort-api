package com.horadocort.domain.repository;

import com.horadocort.domain.entity.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, UUID> {

    List<ServiceCatalog> findAllByActiveTrueOrderByName();

    // Bypass do filtro tenant — usado pelo scheduler cross-tenant
    @Query(value = "SELECT * FROM services WHERE id = :id", nativeQuery = true)
    Optional<ServiceCatalog> findByIdUnfiltered(@Param("id") UUID id);
}
