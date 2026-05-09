package com.example.bookingplan.dto;

import com.example.bookingplan.model.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Role role;
}
