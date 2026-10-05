package com.mayur29.paytmbeat.paytmbeat.entities;

import com.mayur29.paytmbeat.paytmbeat.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String seatNumber;
    @Enumerated(EnumType.STRING)
    private SeatStatus status;
    @Column(name = "reservation_id")
    private String reservationId;
    @Column(name = "hold_id")
    private String holdId;
    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;
}
