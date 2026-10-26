package com.rescue.disaster.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileUpdateDto {
    private String fullName;
    private String phoneNumber;
    private String email;
    private MultipartFile avatarFile;
}