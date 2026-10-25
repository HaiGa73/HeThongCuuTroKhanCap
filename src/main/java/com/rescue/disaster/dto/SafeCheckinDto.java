package com.rescue.disaster.dto;

import lombok.Data;

@Data
public class SafeCheckinDto {
    private String fullName;
    private String phoneNumber;
    private String idCardNumber;
    private String originAddress;
    private Long shelterId;
    private String temporaryAddress;
    private String healthStatus;
}