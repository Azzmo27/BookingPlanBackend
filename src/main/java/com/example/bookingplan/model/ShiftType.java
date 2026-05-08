package com.example.bookingplan.model;

public enum ShiftType {
    DAG_7_15(8),
    DAG_7_14(7),
    DAG_8_14(6),
    AFTEN_15_22(7),
    AFTEN_15_23(8),
    NAT_23_07(8);

    private final int hours;

    ShiftType(int hours) {
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }
}

