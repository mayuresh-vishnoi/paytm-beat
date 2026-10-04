package com.mayur29.paytmbeat.paytmbeat.service;

import com.mayur29.paytmbeat.paytmbeat.dto.request.ShowRequestDTO;
import com.mayur29.paytmbeat.paytmbeat.dto.response.*;
import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import com.mayur29.paytmbeat.paytmbeat.entities.Seats;
import com.mayur29.paytmbeat.paytmbeat.entities.Show;
import com.mayur29.paytmbeat.paytmbeat.enums.SeatStatus;
import com.mayur29.paytmbeat.paytmbeat.exceptions.SeatException;
import com.mayur29.paytmbeat.paytmbeat.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatService seatService;

    public Show addNewShow(ShowRequestDTO showRequestDTO){

        Seats seats = new Seats();
        List<Seat> seatList = showRequestDTO.getSeats().stream().map(seatNumber -> {
            Seat seat = new Seat();
            seat.setSeatNumber(seatNumber);
            seat.setStatus(SeatStatus.AVAILABLE);
            return seat;
        }).collect(Collectors.toList());
        seats.setSeats(seatList);

        Show show = new Show();
        show.setName(showRequestDTO.getName());
        show.setSeats(seats);
        show.setPrice_paise(showRequestDTO.getPrice_paise());
        return showRepository.save(show);
    }

    public ShowStateResponse getShowState(int id) {
        Show show = showRepository.findById(id).orElseThrow(()->new RuntimeException("Show Not Found!!!"));
        var seats = show.getSeats().getSeats();
        List<SeatStateResponse> seatResponses = seats.stream()
                .map(seat -> new SeatStateResponse(
                        seat.getSeatNumber(),
                        seat.getStatus().name()
                ))
                .toList();

        long available = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .count();

        long held = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.HELD)
                .count();

        long confirmed = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.CONFIRMED)
                .count();

        long total = seats.size();

        SeatCountsResponse counts = new SeatCountsResponse(
                available,
                held,
                confirmed,
                total
        );

        return new ShowStateResponse(
                show.getId(),
                show.getName(),
                show.getPrice_paise(),
                total,
                seatResponses,
                counts
        );
    }

    public ShowReserveResponse reserveSeat(
            int showId,
            ShowReserveRequest request) {

        List<String> seatNumbers = request.seats();

        validateSeatNumbers(seatNumbers);

        String requestHash = createRequestHash(showId, seatNumbers);

        Optional<ShowReserveResponse> duplicate =
                seatService.validateDuplicate(
                        request.idempotency_key(),
                        requestHash
                );

        if (duplicate.isPresent()) {
            return duplicate.get();
        }

        return seatService.createFreshBooking(
                showId,
                seatNumbers,
                request.idempotency_key(),
                requestHash
        );
    }

    public String createRequestHash(Integer showId, List<String> seatNumbers) {
        try {
            String canonicalPayload = showId + ":" +
                    seatNumbers.stream()
                            .sorted()
                            .collect(Collectors.joining(","));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    canonicalPayload.getBytes(StandardCharsets.UTF_8)
            );

            return java.util.HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private void validateSeatNumbers(List<String> seatNumbers) {

        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new SeatException(
                    SeatService.SEATSERV_0001,
                    "At least one seat must be selected"
            );
        }

        if (seatNumbers.stream().anyMatch(
                seat -> seat == null || seat.isBlank()
        )) {
            throw new SeatException(
                    SeatService.SEATSERV_0001,
                    "Seat numbers cannot be null or blank"
            );
        }

        if (seatNumbers.size() != seatNumbers.stream().distinct().count()) {
            throw new SeatException(
                    SeatService.SEATSERV_0001,
                    "Duplicate seat numbers are not allowed"
            );
        }
    }
}
