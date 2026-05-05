package com.example.bookingplan.repository;

import com.example.bookingplan.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByOpenTrue();

    List<Shift> findByAssignedUserId(Long userId);

    List<Shift> findByDateBetween(LocalDate start, LocalDate end);
    boolean existsByAssignedUserIdAndDate(Long userId, LocalDate date);
}