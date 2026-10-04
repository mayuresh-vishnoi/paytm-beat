package com.mayur29.paytmbeat.paytmbeat.dto.response;

public record SeatCountsResponse(
        long available,
        long held,
        long confirmed,
        long total
) {}
