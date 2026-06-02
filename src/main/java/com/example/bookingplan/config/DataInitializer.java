package com.example.bookingplan.config;

import com.example.bookingplan.model.Role;
import com.example.bookingplan.model.Team;
import com.example.bookingplan.model.User;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftStatus;
import com.example.bookingplan.repository.ShiftRepository;
import com.example.bookingplan.repository.TeamRepository;
import com.example.bookingplan.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DataInitializer(
            TeamRepository teamRepository,
            UserRepository userRepository,
            ShiftRepository shiftRepository,
            JdbcTemplate jdbcTemplate,
            DataSource dataSource
    ) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        fixShiftUserForeignKeys();

        if (teamRepository.count() == 0) {
            saveTeam("Team 1");
            saveTeam("Team 2");
            saveTeam("Team 3");
        }

        saveUserIfMissing("Vagtplanlægger", "planner@example.com", "planner123", Role.ADMIN,
                "20 10 10 10", "Planvej 1, 1000 København", "Kontor", "70 10 10 10");
        saveUserIfMissing("Afløser Anna", "anna@example.com", "anna123", Role.AFLOSER,
                "22 11 33 44", "Søndergade 12, 8000 Aarhus", "Mette Hansen", "28 44 55 66");
        saveUserIfMissing("Afløser Omar", "omar@example.com", "omar123", Role.AFLOSER,
                "31 22 45 90", "Nørrebrogade 88, 2200 København N", "Sara Ali", "26 77 88 99");

        migrateMissingShiftStatuses();
    }

    private void saveTeam(String name) {
        Team team = new Team();
        team.setName(name);
        teamRepository.save(team);
    }

    private void saveUserIfMissing(
            String name,
            String email,
            String password,
            Role role,
            String phone,
            String address,
            String emergencyContactName,
            String emergencyContactPhone
    ) {
        var existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean changed = false;

            if (user.getPhone() == null) {
                user.setPhone(phone);
                changed = true;
            }
            if (user.getAddress() == null) {
                user.setAddress(address);
                changed = true;
            }
            if (user.getEmergencyContactName() == null) {
                user.setEmergencyContactName(emergencyContactName);
                changed = true;
            }
            if (user.getEmergencyContactPhone() == null) {
                user.setEmergencyContactPhone(emergencyContactPhone);
                changed = true;
            }

            if (changed) {
                userRepository.save(user);
            }
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setPhone(phone);
        user.setAddress(address);
        user.setEmergencyContactName(emergencyContactName);
        user.setEmergencyContactPhone(emergencyContactPhone);
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

    private void fixShiftUserForeignKeys() {
        if (!isMySql()) {
            return;
        }

        fixShiftUserForeignKey("assigned_user_id", "fk_shift_assigned_user_app_user");
        fixShiftUserForeignKey("requested_user_id", "fk_shift_requested_user_app_user");
    }

    private boolean isMySql() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            return metadata.getDatabaseProductName().toLowerCase().contains("mysql");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not inspect database metadata", exception);
        }
    }

    private void fixShiftUserForeignKey(String columnName, String newConstraintName) {
        String sql = """
                select constraint_name
                from information_schema.key_column_usage
                where table_schema = database()
                  and table_name = 'shift'
                  and column_name = ?
                  and referenced_table_name is not null
                  and referenced_table_name <> 'app_user'
                """;

        List<String> wrongConstraints = jdbcTemplate.queryForList(sql, String.class, columnName);

        for (String constraintName : wrongConstraints) {
            jdbcTemplate.execute("alter table shift drop foreign key `" + constraintName + "`");
        }

        if (!wrongConstraints.isEmpty() && !foreignKeyExists(newConstraintName)) {
            jdbcTemplate.execute(
                    "alter table shift add constraint " + newConstraintName
                            + " foreign key (" + columnName + ") references app_user(id)"
            );
        }
    }

    private boolean foreignKeyExists(String constraintName) {
        String sql = """
                select count(*)
                from information_schema.table_constraints
                where table_schema = database()
                  and table_name = 'shift'
                  and constraint_type = 'FOREIGN KEY'
                  and constraint_name = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, constraintName);
        return count != null && count > 0;
    }
}
