package com.rescue.disaster.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String email;
}