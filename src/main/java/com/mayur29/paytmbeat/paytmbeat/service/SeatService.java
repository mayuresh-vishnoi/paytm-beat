package com.mayur29.paytmbeat.paytmbeat.service;

import com.mayur29.paytmbeat.paytmbeat.dto.response.ShowReserveResponse;
import com.mayur29.paytmbeat.paytmbeat.entities.IdempotencyRecord;
import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import com.mayur29.paytmbeat.paytmbeat.entities.Show;
import com.mayur29.paytmbeat.paytmbeat.enums.IdempotencyStatus;
import com.mayur29.paytmbeat.paytmbeat.enums.SeatStatus;
import com.mayur29.paytmbeat.paytmbeat.exceptions.SeatException;
import com.mayur29.paytmbeat.paytmbeat.repositories.IdempotencyRepository;
import com.mayur29.paytmbeat.paytmbeat.repositories.SeatRepository;
import com.mayur29.paytmbeat.paytmbeat.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeatService {

    public static final String SEATSERV_0001 = "SEATSERV0001";
    public static final String SEATSERV_0002 = "SEATSERV0002";

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    public Optional<ShowReserveResponse> validateDuplicate(
            String idempotentKey,
            String requestHash) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userId = authentication.getName();


        Optional<IdempotencyRecord> existingRecord =
                idempotencyRepository
                        .findRecordByIdempotencyKeyAndUserId(idempotentKey, userId);

        if (existingRecord.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyRecord record = existingRecord.get();

        if (!record.getRequestHash().equals(requestHash)) {
            throw new SeatException(
                    SEATSERV_0002,
                    "Idempotency key was already used with a different request"
            );
        }

        if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new SeatException(
                    SEATSERV_0002,
                    "Request with this idempotency key is still in progress"
            );
        }

        ShowReserveResponse response = new ShowReserveResponse();

        response.setShow_id(record.getShowId());
        response.setReservation_id(record.getReservationId());
        response.setUser_id(userId);
        response.setStatus(record.getStatus());

        List<String> seatNumbers =
                seatRepository.findSeatNumbersByReservationId(
                        record.getReservationId()
                );

        response.setSeats(seatNumbers);

        Show show = showRepository.findById(record.getShowId())
                .orElseThrow(() -> new IllegalStateException("Show not found"));

        response.setAmount_paise(show.getPrice_paise() * seatNumbers.size());

        return Optional.of(response);
    }

    @Transactional
    public ShowReserveResponse createFreshBooking(
            int showId,
            List<String> seatNumbers,
            String idempotencyKey,
            String requestHash) {

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        IdempotencyRecord record = new IdempotencyRecord();
        record.setUserId(userId);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setShowId(showId);
        record.setStatus(IdempotencyStatus.IN_PROGRESS);

        idempotencyRepository.saveAndFlush(record);

        List<Seat> seats =
                seatRepository.findSeatsForUpdate(showId, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            throw new SeatException(
                    SEATSERV_0001,
                    "One or more seats do not exist"
            );
        }

        boolean unavailable = seats.stream()
                .anyMatch(seat -> seat.getStatus() != SeatStatus.AVAILABLE);

        if (unavailable) {
            throw new SeatException(
                    SEATSERV_0002,
                    "One or more seats are unavailable"
            );
        }

        String reservationId = UUID.randomUUID().toString();

        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.HELD);
            seat.setReservationId(reservationId);
        });

        record.setReservationId(reservationId);
        record.setStatus(IdempotencyStatus.COMPLETED);

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));

        ShowReserveResponse response = new ShowReserveResponse();
        response.setShow_id(showId);
        response.setReservation_id(reservationId);
        response.setUser_id(userId);
        response.setSeats(seatNumbers);
        response.setStatus(IdempotencyStatus.COMPLETED);
        response.setAmount_paise(show.getPrice_paise() * seatNumbers.size());

        return response;
    }
}
