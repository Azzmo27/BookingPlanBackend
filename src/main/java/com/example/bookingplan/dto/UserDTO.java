package com.example.bookingplan.dto;

import com.example.bookingplan.model.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
}
