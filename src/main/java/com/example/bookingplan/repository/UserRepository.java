package com.example.bookingplan.repository;


import com.example.bookingplan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}