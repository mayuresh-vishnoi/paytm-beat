package com.mayur29.paytmbeat.paytmbeat.repositories;

import com.mayur29.paytmbeat.paytmbeat.entities.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord,Integer> {
    Optional<IdempotencyRecord> findRecordByIdempotencyKeyAndUserId(String idempotencyKey, String userId);
}
