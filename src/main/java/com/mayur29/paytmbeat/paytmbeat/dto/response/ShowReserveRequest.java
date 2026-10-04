package com.mayur29.paytmbeat.paytmbeat.dto.response;

import com.mayur29.paytmbeat.paytmbeat.entities.Seat;

import java.util.List;

public record ShowReserveRequest(
        List<String> seats,
        String idempotency_key
) {}
