package com.example.bookingplan.dto;



import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateShiftDTO {
    private LocalDate date;
    private String type;
    private Long teamId;
}