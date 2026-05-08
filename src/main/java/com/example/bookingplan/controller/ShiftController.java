package com.example.bookingplan.controller;


import com.example.bookingplan.dto.ApplyShiftDTO;
import com.example.bookingplan.dto.CreateShiftDTO;
import com.example.bookingplan.dto.ShiftDTO;
import com.example.bookingplan.mapper.ShiftMapper;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftType;
import com.example.bookingplan.model.Team;
import com.example.bookingplan.repository.TeamRepository;
import com.example.bookingplan.service.ShiftService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService, TeamRepository teamRepository) {
        this.shiftService = shiftService;
        this.teamRepository = teamRepository;
    }

    private final TeamRepository teamRepository;

    @GetMapping("/open")
    public List<ShiftDTO> getOpenShifts() {
        return shiftService.getOpenShifts();
    }

    @GetMapping("/user/{userId}")
    public List<ShiftDTO> getUserShifts(@PathVariable Long userId) {
        return shiftService.getUserShifts(userId);
    }

    @GetMapping("/week")
    public List<ShiftDTO> getWeekPlan(@RequestParam String start) {
        return shiftService.getWeekPlan(LocalDate.parse(start));
    }

    @GetMapping("/pending")
    public List<ShiftDTO> getPendingRequests() {
        return shiftService.getPendingRequests();
    }

    @PostMapping("/{shiftId}/apply")
    public ShiftDTO apply(@PathVariable Long shiftId,
                          @RequestBody ApplyShiftDTO dto) {

        return ShiftMapper.toDTO(
                shiftService.applyForShift(shiftId, dto.getUserId())
        );
    }

    @PostMapping("/{shiftId}/approve")
    public ShiftDTO approve(@PathVariable Long shiftId) {
        return shiftService.toDtoWithWarnings(shiftService.approveShift(shiftId));
    }

    @PostMapping("/{shiftId}/reject")
    public ShiftDTO reject(@PathVariable Long shiftId) {
        return shiftService.toDtoWithWarnings(shiftService.rejectShift(shiftId));
    }

    @PostMapping
    public ShiftDTO createShift(@RequestBody CreateShiftDTO dto) {

        Shift shift = new Shift();
        shift.setDate(dto.getDate());
        shift.setType(ShiftType.valueOf(dto.getType()));

        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow();

        shift.setTeam(team);

        return ShiftMapper.toDTO(
                shiftService.createShift(shift)
        );
    }
}
