package com.rescue.disaster.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MissingPersonDto {
    private String targetName;
    private String gender;
    private Integer age;
    private String lastSeenLocation;
    private String identifyingCharacteristics;
    private String contactPerson;
    private String contactPhone;
    private MultipartFile photoFile; // Ảnh chân dung upload Cloudinary
}