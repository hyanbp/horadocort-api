package com.horadocort.domain.repository;

import com.horadocort.domain.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarberRepository extends JpaRepository<Barber, UUID> {

    List<Barber> findAllByActiveTrueOrderByName();

    // Bypass do filtro tenant — usado pelo scheduler cross-tenant
    @Query(value = "SELECT * FROM barbers WHERE id = :id", nativeQuery = true)
    Optional<Barber> findByIdUnfiltered(@Param("id") UUID id);
}
