package com.example.bookingplan.service;

import com.example.bookingplan.dto.ShiftDTO;
import com.example.bookingplan.exception.BadRequestException;
import com.example.bookingplan.exception.NotFoundException;
import com.example.bookingplan.mapper.ShiftMapper;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftStatus;
import com.example.bookingplan.model.ShiftType;
import com.example.bookingplan.model.Team;
import com.example.bookingplan.model.User;
import com.example.bookingplan.repository.ShiftRepository;
import com.example.bookingplan.repository.TeamRepository;
import com.example.bookingplan.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository, TeamRepository teamRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public List<ShiftDTO> getOpenShifts() {
        return shiftRepository.findByOpenTrue()
                .stream()
                .map(this::toDtoWithWarnings)
                .toList();
    }

    public List<ShiftDTO> getUserShifts(Long userId) {
        return shiftRepository.findByAssignedUserId(userId)
                .stream()
                .map(this::toDtoWithWarnings)
                .toList();
    }

    public List<ShiftDTO> getWeekPlan(LocalDate start) {
        return shiftRepository.findByDateBetween(start, start.plusDays(6))
                .stream()
                .map(this::toDtoWithWarnings)
                .toList();
    }

    public List<ShiftDTO> getPendingRequests() {
        return shiftRepository.findByStatus(ShiftStatus.REQUESTED)
                .stream()
                .map(this::toDtoWithWarnings)
                .toList();
    }

    public Shift applyForShift(Long shiftId, Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Shift not found"));

        if ((shift.getStatus() != null && shift.getStatus() != ShiftStatus.OPEN) || !shift.isOpen()) {
            throw new BadRequestException("Shift is not open");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateNoDoubleBooking(userId, shift.getDate());

        shift.setRequestedUser(user);
        shift.setStatus(ShiftStatus.REQUESTED);
        shift.setOpen(false);

        return shiftRepository.save(shift);
    }

    public Shift approveShift(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Shift not found"));

        if (shift.getStatus() != ShiftStatus.REQUESTED || shift.getRequestedUser() == null) {
            throw new BadRequestException("Shift has no pending request");
        }

        Long userId = shift.getRequestedUser().getId();
        if (shiftRepository.existsByAssignedUserIdAndDate(userId, shift.getDate())) {
            throw new BadRequestException("User already has an approved shift this day");
        }

        shift.setAssignedUser(shift.getRequestedUser());
        shift.setRequestedUser(null);
        shift.setStatus(ShiftStatus.APPROVED);
        shift.setOpen(false);

        return shiftRepository.save(shift);
    }

    public Shift rejectShift(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Shift not found"));

        if (shift.getStatus() != ShiftStatus.REQUESTED) {
            throw new BadRequestException("Only pending requests can be rejected");
        }

        shift.setRequestedUser(null);
        shift.setStatus(ShiftStatus.OPEN);
        shift.setOpen(true);

        return shiftRepository.save(shift);
    }

    public Shift createShift(Shift shift) {
        shift.setOpen(true);
        shift.setStatus(ShiftStatus.OPEN);
        return shiftRepository.save(shift);
    }

    public Shift updateShift(Long shiftId, LocalDate date, String type, Long teamId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Shift not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Team not found"));

        shift.setDate(date);
        shift.setType(ShiftType.valueOf(type));
        shift.setTeam(team);

        return shiftRepository.save(shift);
    }

    public void deleteShift(Long shiftId) {
        if (!shiftRepository.existsById(shiftId)) {
            throw new NotFoundException("Shift not found");
        }

        shiftRepository.deleteById(shiftId);
    }

    public ShiftDTO toDtoWithWarnings(Shift shift) {
        ShiftDTO dto = ShiftMapper.toDTO(shift);

        if (shift.getRequestedUser() != null) {
            int weekHours = approvedWeekHours(shift.getRequestedUser().getId(), shift.getDate());
            int weekHoursIfApproved = weekHours + shift.getType().getHours();

            dto.setRequestedUserWeekHours(weekHours);
            dto.setRequestedUserWeekHoursIfApproved(weekHoursIfApproved);
            dto.setExceeds37Hours(weekHoursIfApproved > 37);
        }

        return dto;
    }

    private void validateNoDoubleBooking(Long userId, LocalDate date) {
        boolean alreadyApproved = shiftRepository.existsByAssignedUserIdAndDate(userId, date);
        boolean alreadyRequested = shiftRepository.existsByRequestedUserIdAndDate(userId, date);

        if (alreadyApproved || alreadyRequested) {
            throw new BadRequestException("User already has or requested a shift this day");
        }
    }

    private int approvedWeekHours(Long userId, LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        return shiftRepository.findByAssignedUserIdAndDateBetween(userId, weekStart, weekEnd)
                .stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.APPROVED)
                .mapToInt(shift -> shift.getType().getHours())
                .sum();
    }
}
