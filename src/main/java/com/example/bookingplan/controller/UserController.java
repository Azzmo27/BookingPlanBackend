package com.example.bookingplan.controller;

import com.example.bookingplan.dto.UserDTO;
import com.example.bookingplan.exception.NotFoundException;
import com.example.bookingplan.mapper.UserMapper;
import com.example.bookingplan.model.Shift;
import com.example.bookingplan.model.ShiftStatus;
import com.example.bookingplan.repository.ShiftRepository;
import com.example.bookingplan.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    public UserController(UserRepository userRepository, ShiftRepository shiftRepository) {
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
    }

    @GetMapping
    public List<UserDTO> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        for (Shift shift : shiftRepository.findByAssignedUserId(userId)) {
            shift.setAssignedUser(null);
            shift.setStatus(ShiftStatus.OPEN);
            shift.setOpen(true);
            shiftRepository.save(shift);
        }

        for (Shift shift : shiftRepository.findByRequestedUserId(userId)) {
            shift.setRequestedUser(null);
            shift.setStatus(ShiftStatus.OPEN);
            shift.setOpen(true);
            shiftRepository.save(shift);
        }

        userRepository.deleteById(userId);
    }
}
