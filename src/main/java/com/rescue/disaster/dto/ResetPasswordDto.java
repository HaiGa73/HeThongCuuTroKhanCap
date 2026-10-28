package com.rescue.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordDto {
    private String username;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}