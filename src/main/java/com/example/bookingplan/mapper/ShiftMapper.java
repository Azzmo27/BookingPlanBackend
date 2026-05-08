package com.example.bookingplan.mapper;


import com.example.bookingplan.dto.ShiftDTO;
import com.example.bookingplan.model.Shift;

public class ShiftMapper {

    public static ShiftDTO toDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();

        dto.setId(shift.getId());
        dto.setDate(shift.getDate());
        dto.setType(shift.getType().name());
        dto.setHours(shift.getType().getHours());
        dto.setStatus(shift.getStatus() == null ? null : shift.getStatus().name());
        dto.setOpen(shift.isOpen());

        if (shift.getTeam() != null) {
            dto.setTeamName(shift.getTeam().getName());
        }

        if (shift.getAssignedUser() != null) {
            dto.setUserName(shift.getAssignedUser().getName());
        }

        if (shift.getRequestedUser() != null) {
            dto.setRequestedUserId(shift.getRequestedUser().getId());
            dto.setRequestedUserName(shift.getRequestedUser().getName());
        }

        return dto;
    }
}
