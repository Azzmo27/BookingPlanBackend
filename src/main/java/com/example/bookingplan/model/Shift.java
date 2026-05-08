package com.example.bookingplan.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private ShiftType type;

    @ManyToOne
    private Team team;

    @ManyToOne
    private User assignedUser;

    @ManyToOne
    private User requestedUser;

    @Enumerated(EnumType.STRING)
    private ShiftStatus status;

    private boolean open;
}
