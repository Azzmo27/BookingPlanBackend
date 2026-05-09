package com.example.bookingplan.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    private Role role;
}
