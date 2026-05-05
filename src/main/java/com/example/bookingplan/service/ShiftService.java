package com.example.bookingplan.service;


import com.example.bookingplan.dto.ShiftDTO;
import com.example.bookingplan.mapper.ShiftMapper;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.User;
import com.example.bookingplan.repository.ShiftRepository;
import com.example.bookingplan.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public List<ShiftDTO> getOpenShifts() {
        return shiftRepository.findByOpenTrue()
                .stream()
                .map(ShiftMapper::toDTO)
                .toList();
    }

    public List<Shift> getUserShifts(Long userId) {
        return shiftRepository.findByAssignedUserId(userId);
    }

    public List<Shift> getWeekPlan(LocalDate start) {
        return shiftRepository.findByDateBetween(start, start.plusDays(7));
    }

    public Shift applyForShift(Long shiftId, Long userId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        if (!shift.isOpen()) {
            throw new RuntimeException("Shift already taken");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🚨 NY REGEL
        boolean alreadyHasShift = shiftRepository
                .existsByAssignedUserIdAndDate(userId, shift.getDate());

        if (alreadyHasShift) {
            throw new RuntimeException("User already has a shift this day");
        }

        shift.setAssignedUser(user);
        shift.setOpen(false);

        return shiftRepository.save(shift);
    }
    public Shift createShift(Shift shift) {
        shift.setOpen(true);
        return shiftRepository.save(shift);
    }


}