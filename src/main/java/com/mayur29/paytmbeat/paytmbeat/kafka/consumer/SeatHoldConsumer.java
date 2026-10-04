package com.mayur29.paytmbeat.paytmbeat.kafka.consumer;

import com.mayur29.paytmbeat.paytmbeat.dto.request.SeatHoldEvent;
import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import com.mayur29.paytmbeat.paytmbeat.enums.SeatStatus;
import com.mayur29.paytmbeat.paytmbeat.repositories.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatHoldConsumer {

    private final SeatRepository seatRepository;
    private final TaskScheduler taskScheduler;

    @KafkaListener(
            topics = "Paytm-beat-hold-tickets",
            groupId = "paytm_beats_consumer_group"
    )
    public void consume(SeatHoldEvent event) {

        taskScheduler.schedule(
                () -> checkSeatStatus(event),
                Instant.now().plus(1, ChronoUnit.MINUTES)
        );
    }

    private void checkSeatStatus(SeatHoldEvent event) {
        List<String> seatNumbers = new ArrayList<>();
        System.out.println("event ::"+event);
        for (Seat seat1 : event.seatNumbers()) {
            seatNumbers.add(seat1.getSeatNumber());
        }
        System.out.println("seatNumbers ::"+seatNumbers);
        List<Seat> seats = seatRepository
                .findSeatsForUpdate(
                        event.showId(),
                        seatNumbers
                );

        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.HELD) {
                System.out.println("updating hold to available ::"+seat);
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.updateStatusToAvailable(seat.getId());
            }
        }
    }
}
