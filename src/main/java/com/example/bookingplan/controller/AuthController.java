package com.example.bookingplan.controller;

import com.example.bookingplan.dto.LoginDTO;
import com.example.bookingplan.dto.UserDTO;
import com.example.bookingplan.exception.BadRequestException;
import com.example.bookingplan.mapper.UserMapper;
import com.example.bookingplan.model.User;
import com.example.bookingplan.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public UserDTO login(@RequestBody LoginDTO dto) {
        User user = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getPassword() == null || !user.getPassword().equals(dto.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        return UserMapper.toDTO(user);
    }
}
