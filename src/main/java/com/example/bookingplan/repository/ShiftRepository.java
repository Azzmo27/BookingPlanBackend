package com.example.bookingplan.repository;

import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByOpenTrue();

    List<Shift> findByAssignedUserId(Long userId);

    List<Shift> findByDateBetween(LocalDate start, LocalDate end);
    boolean existsByAssignedUserIdAndDate(Long userId, LocalDate date);
    boolean existsByRequestedUserIdAndDate(Long userId, LocalDate date);
    List<Shift> findByStatus(ShiftStatus status);
    List<Shift> findByAssignedUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
