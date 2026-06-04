package com.horadocort.domain.repository;

import com.horadocort.domain.entity.Booking;
import com.horadocort.domain.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.barberId = :barberId
              AND b.status <> com.horadocort.domain.entity.BookingStatus.CANCELLED
              AND b.startAt < :endOfDay
              AND b.endAt > :startOfDay
            ORDER BY b.startAt
            """)
    List<Booking> findActiveByBarberAndDay(
            @Param("barberId") UUID barberId,
            @Param("startOfDay") ZonedDateTime startOfDay,
            @Param("endOfDay") ZonedDateTime endOfDay
    );

    // Para o scheduler de lembretes — busca de todos tenants
    // Bypass do filtro: usa native query
    @Query(value = """
            SELECT * FROM bookings
            WHERE status = :status
              AND reminder_sent = false
              AND start_at BETWEEN :from AND :to
            """, nativeQuery = true)
    List<Booking> findPendingRemindersAcrossTenants(
            @Param("status") String status,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to
    );
}
