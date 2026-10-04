package com.mayur29.paytmbeat.paytmbeat.dto.request;

import com.mayur29.paytmbeat.paytmbeat.entities.Seat;

import java.util.List;

public record SeatHoldEvent(
        Integer showId,
        List<Seat> seatNumbers,
        String holdId
) {}