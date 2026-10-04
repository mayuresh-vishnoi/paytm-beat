package com.mayur29.paytmbeat.paytmbeat.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "SEATS")
@Data
public class Seats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "seats_group_id")
    private List<Seat> seats;
}
