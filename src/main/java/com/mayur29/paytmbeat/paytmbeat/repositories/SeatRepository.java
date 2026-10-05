package com.mayur29.paytmbeat.paytmbeat.repositories;

import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat,Integer> {

    @Query(value = """
    SELECT s.*
    FROM seat s
    INNER JOIN shows ss ON s.seats_group_id = ss.seats_id
    WHERE ss.id = :showId
      AND s.seat_number IN (:seatNumbers)
    ORDER BY s.seat_number
    FOR UPDATE OF s
    """, nativeQuery = true)
    List<Seat> findSeatsForUpdate(
            @Param("showId") Integer showId,
            @Param("seatNumbers") List<String> seatNumbers
    );

    @Query(value = "UPDATE seat SET status = 'AVAILABLE' WHERE id = :id",nativeQuery = true)
    void updateStatusToAvailable(@Param("id") int id);

    @Query("""
    SELECT s.seatNumber
    FROM Seat s
    WHERE s.reservationId = :reservationId""")
    List<String> findSeatNumbersByReservationId(
            @Param("reservationId") String reservationId
    );

    @Modifying
    @Query(value = """
    UPDATE seat
    SET status = 'AVAILABLE',
        hold_id = NULL,
        hold_expires_at = NULL
    WHERE hold_id = :holdId
      AND status = 'HELD'
      AND hold_expires_at <= CURRENT_TIMESTAMP
    """, nativeQuery = true)
    int expireHold(@Param("holdId") String holdId);

    @Query(value = """
    SELECT DISTINCT hold_id
    FROM seat
    WHERE status = 'HELD'
      AND hold_id IS NOT NULL
      AND hold_expires_at <= CURRENT_TIMESTAMP
    """, nativeQuery = true)
    List<String> findExpiredHoldIds();

    @Query(value = """
    SELECT COUNT(*)
    FROM seat s
    JOIN shows sh ON s.seats_group_id = sh.seats_id
    WHERE sh.id = ?1
      AND s.reservation_id IN (
          SELECT ir.reservation_id
          FROM idempotency_records ir
          WHERE ir.user_id = ?2
      )
      AND s.status = 'HELD'
""", nativeQuery = true)
    long countHeldSeatsForUser(
            @Param("showId") Integer showId,
            @Param("userId") String userId
    );
}
