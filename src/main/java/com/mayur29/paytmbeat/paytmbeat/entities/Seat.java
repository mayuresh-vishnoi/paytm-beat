package com.mayur29.paytmbeat.paytmbeat.entities;

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
    private String status;
}
