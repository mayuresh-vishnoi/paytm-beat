package com.mayur29.paytmbeat.paytmbeat.dto.response;

import java.util.List;

public record ShowStateResponse(
        Integer showId,
        String name,
        Integer pricePaise,
        long totalSeats,
        List<SeatStateResponse> seats,
        SeatCountsResponse counts
) {}
