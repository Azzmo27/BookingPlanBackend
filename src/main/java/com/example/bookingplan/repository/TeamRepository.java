package com.example.bookingplan.repository;
import com.example.bookingplan.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}