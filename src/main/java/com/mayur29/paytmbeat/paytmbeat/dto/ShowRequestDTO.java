package com.mayur29.paytmbeat.paytmbeat.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShowRequestDTO {
    private String name;
    private List<String> seats;
    private Integer price_paise;
}
