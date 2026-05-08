package com.example.bookingplan.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class ShiftDTO {
    private Long id;
    private LocalDate date;
    private String type;
    private String teamName;
    private String userName;
    private String requestedUserName;
    private Long requestedUserId;
    private String status;
    private int hours;
    private int requestedUserWeekHours;
    private int requestedUserWeekHoursIfApproved;
    private boolean exceeds37Hours;
    private boolean open;
}
