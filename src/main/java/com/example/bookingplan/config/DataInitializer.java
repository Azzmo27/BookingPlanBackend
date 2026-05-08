package com.example.bookingplan.config;

import com.example.bookingplan.model.Role;
import com.example.bookingplan.model.Team;
import com.example.bookingplan.model.User;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftStatus;
import com.example.bookingplan.repository.ShiftRepository;
import com.example.bookingplan.repository.TeamRepository;
import com.example.bookingplan.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    public DataInitializer(TeamRepository teamRepository, UserRepository userRepository, ShiftRepository shiftRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public void run(String... args) {
        if (teamRepository.count() == 0) {
            saveTeam("Team 1");
            saveTeam("Team 2");
            saveTeam("Team 3");
        }

        saveUserIfMissing("Vagtplanlaegger", "planner@example.com", "planner123", Role.ADMIN);
        saveUserIfMissing("Afloser Anna", "anna@example.com", "anna123", Role.AFLOSER);
        saveUserIfMissing("Afloser Omar", "omar@example.com", "omar123", Role.AFLOSER);

        migrateMissingShiftStatuses();
    }

    private void saveTeam(String name) {
        Team team = new Team();
        team.setName(name);
        teamRepository.save(team);
    }

    private void saveUserIfMissing(String name, String email, String password, Role role) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        userRepository.save(user);
    }

    private void migrateMissingShiftStatuses() {
        for (Shift shift : shiftRepository.findAll()) {
            if (shift.getStatus() != null) {
                continue;
            }

            if (shift.getAssignedUser() != null) {
                shift.setStatus(ShiftStatus.APPROVED);
                shift.setOpen(false);
            } else {
                shift.setStatus(ShiftStatus.OPEN);
                shift.setOpen(true);
            }

            shiftRepository.save(shift);
        }
    }
}
