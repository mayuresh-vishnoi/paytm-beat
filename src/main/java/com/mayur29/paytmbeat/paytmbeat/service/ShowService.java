package com.mayur29.paytmbeat.paytmbeat.service;

import com.mayur29.paytmbeat.paytmbeat.dto.ShowRequestDTO;
import com.mayur29.paytmbeat.paytmbeat.entities.Seat;
import com.mayur29.paytmbeat.paytmbeat.entities.Seats;
import com.mayur29.paytmbeat.paytmbeat.entities.Show;
import com.mayur29.paytmbeat.paytmbeat.repositories.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
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
            seat.setStatus("AVAILABLE");
            return seat;
        }).collect(Collectors.toList());
        seats.setSeats(seatList);

        Show show = new Show();
        show.setName(showRequestDTO.getName());
        show.setSeats(seats);
        return showRepository.save(show);
    }

    public Show getShowState(int id) {
        return showRepository.findById(id).orElse(null);
    }
}
