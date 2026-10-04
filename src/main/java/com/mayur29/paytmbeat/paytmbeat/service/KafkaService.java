package com.mayur29.paytmbeat.paytmbeat.service;

import com.mayur29.paytmbeat.paytmbeat.dto.request.SeatHoldEvent;
import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaService {

    private final KafkaTemplate<String, SeatHoldEvent> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, SeatHoldEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishKafkaEvent(List<Seat> seats,String idempotentKey,int showId) {
        SeatHoldEvent seatHoldEvent = new SeatHoldEvent(showId,seats,idempotentKey);
        kafkaTemplate.send("Paytm-beat-hold-tickets",idempotentKey, seatHoldEvent);
    }
}
