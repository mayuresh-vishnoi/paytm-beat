package com.mayur29.paytmbeat.paytmbeat.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "shows")
@Data
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "seats_id")
    private Seats seats;
    private Integer price_paise;
}
