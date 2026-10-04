package com.mayur29.paytmbeat.paytmbeat.dto.response;

import com.mayur29.paytmbeat.paytmbeat.enums.IdempotencyStatus;
import lombok.Data;

import java.util.List;

@Data
public class ShowReserveResponse {
    private String reservation_id;
    private int show_id;
    private String user_id;
    private List<String> seats;
    private Integer amount_paise;
    private IdempotencyStatus status;
}
