package com.mayur29.paytmbeat.paytmbeat.entities;


import jakarta.persistence.*;
import lombok.Data;

@Table(name = "BOOKING")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int bookingId;
    private String idempotentKey;
    private String seatNumber;
    private String status;
}
