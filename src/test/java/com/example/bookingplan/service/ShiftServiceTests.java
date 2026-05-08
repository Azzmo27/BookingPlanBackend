package com.example.bookingplan.service;

import com.example.bookingplan.dto.ShiftDTO;
import com.example.bookingplan.model.Role;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftStatus;
import com.example.bookingplan.model.ShiftType;
import com.example.bookingplan.model.Team;
import com.example.bookingplan.model.User;
import com.example.bookingplan.repository.ShiftRepository;
import com.example.bookingplan.repository.TeamRepository;
import com.example.bookingplan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ShiftServiceTests {

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private Team team;
    private User user;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
        userRepository.deleteAll();
        teamRepository.deleteAll();

        team = new Team();
        team.setName("Team 1");
        team = teamRepository.save(team);

        user = new User();
        user.setName("Azra");
        user.setEmail("azra@example.com");
        user.setPassword("secret");
        user.setRole(Role.AFLOSER);
        user = userRepository.save(user);
    }

    @Test
    void createShiftMarksItOpen() {
        Shift shift = new Shift();
        shift.setDate(LocalDate.of(2026, 5, 8));
        shift.setType(ShiftType.DAG_7_15);
        shift.setTeam(team);

        Shift saved = shiftService.createShift(shift);

        assertTrue(saved.isOpen());
        assertEquals(ShiftStatus.OPEN, saved.getStatus());
        assertEquals(team.getId(), saved.getTeam().getId());
    }

    @Test
    void applyForShiftCreatesPendingRequest() {
        Shift shift = openShift(LocalDate.of(2026, 5, 8));

        Shift applied = shiftService.applyForShift(shift.getId(), user.getId());

        assertFalse(applied.isOpen());
        assertEquals(ShiftStatus.REQUESTED, applied.getStatus());
        assertEquals(user.getId(), applied.getRequestedUser().getId());
        assertNull(applied.getAssignedUser());
    }

    @Test
    void approveShiftAssignsRequestedUser() {
        Shift shift = openShift(LocalDate.of(2026, 5, 8));
        Shift requested = shiftService.applyForShift(shift.getId(), user.getId());

        Shift approved = shiftService.approveShift(requested.getId());

        assertEquals(ShiftStatus.APPROVED, approved.getStatus());
        assertEquals(user.getId(), approved.getAssignedUser().getId());
    }

    @Test
    void applyForShiftRejectsUserWithShiftRequestOnSameDate() {
        LocalDate date = LocalDate.of(2026, 5, 8);
        Shift requested = openShift(date);
        shiftService.applyForShift(requested.getId(), user.getId());

        Shift second = openShift(date);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> shiftService.applyForShift(second.getId(), user.getId())
        );

        assertEquals("User already has or requested a shift this day", exception.getMessage());
    }

    @Test
    void getWeekPlanReturnsSevenDayRange() {
        LocalDate start = LocalDate.of(2026, 5, 4);
        openShift(start);
        openShift(start.plusDays(6));
        openShift(start.plusDays(7));

        List<ShiftDTO> weekPlan = shiftService.getWeekPlan(start);

        assertEquals(2, weekPlan.size());
    }

    @Test
    void pendingRequestWarnsWhenApprovalWouldExceed37Hours() {
        LocalDate monday = LocalDate.of(2026, 5, 4);
        for (int i = 0; i < 4; i++) {
            Shift shift = openShift(monday.plusDays(i));
            shiftService.approveShift(shiftService.applyForShift(shift.getId(), user.getId()).getId());
        }

        Shift requested = openShift(monday.plusDays(4));
        shiftService.applyForShift(requested.getId(), user.getId());

        ShiftDTO dto = shiftService.getPendingRequests().getFirst();

        assertEquals(32, dto.getRequestedUserWeekHours());
        assertEquals(40, dto.getRequestedUserWeekHoursIfApproved());
        assertTrue(dto.isExceeds37Hours());
    }

    private Shift openShift(LocalDate date) {
        Shift shift = new Shift();
        shift.setDate(date);
        shift.setType(ShiftType.DAG_7_15);
        shift.setTeam(team);
        shift.setOpen(true);
        shift.setStatus(ShiftStatus.OPEN);
        return shiftRepository.save(shift);
    }
}
