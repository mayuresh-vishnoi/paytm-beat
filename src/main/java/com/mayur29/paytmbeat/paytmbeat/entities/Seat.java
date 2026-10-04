package com.mayur29.paytmbeat.paytmbeat.entities;

import com.mayur29.paytmbeat.paytmbeat.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Data;

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
}
