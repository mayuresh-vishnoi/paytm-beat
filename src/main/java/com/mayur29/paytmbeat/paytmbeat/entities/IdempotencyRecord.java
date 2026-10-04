package com.mayur29.paytmbeat.paytmbeat.entities;

import com.mayur29.paytmbeat.paytmbeat.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "idempotency_key"}
        )
)
@Getter
@Setter
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "reservation_id")
    private String reservationId;

    @Column(name = "show_id")
    private Integer showId;

    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;
}
