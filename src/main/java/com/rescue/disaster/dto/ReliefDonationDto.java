package com.rescue.disaster.dto;

import lombok.Data;

@Data
public class ReliefDonationDto {
    private String donorName;
    private String phoneNumber;
    private String organization;
    private String supplyType;
    private String quantityDetail;
    private Long destinationShelterId;
    private String note;
}