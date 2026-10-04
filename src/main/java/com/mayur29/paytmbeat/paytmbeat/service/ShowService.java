package com.mayur29.paytmbeat.paytmbeat.service;

import com.mayur29.paytmbeat.paytmbeat.dto.request.ShowRequestDTO;
import com.mayur29.paytmbeat.paytmbeat.dto.response.SeatCountsResponse;
import com.mayur29.paytmbeat.paytmbeat.dto.response.SeatStateResponse;
import com.mayur29.paytmbeat.paytmbeat.dto.response.ShowStateResponse;
import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import com.mayur29.paytmbeat.paytmbeat.entities.Seats;
import com.mayur29.paytmbeat.paytmbeat.entities.Show;
import com.mayur29.paytmbeat.paytmbeat.enums.SeatStatus;
import com.mayur29.paytmbeat.paytmbeat.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShowService {

    @Autowired
    private ShowRepository showRepository;

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
}
