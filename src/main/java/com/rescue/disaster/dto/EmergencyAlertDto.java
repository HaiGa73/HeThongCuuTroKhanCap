package com.rescue.disaster.dto;

import lombok.Data;

@Data
public class EmergencyAlertDto {
    private String title;
    private String content;
    private String severity;
    private Integer regionId;
}