package com.rescue.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SosCreateRequestDto {
    private String senderName;
    private String phoneNumber;
    private String latitude;
    private String longitude;
    private String addressDetail;
    private Integer numberOfPeople;
    private String priority;
    private String description;
    private List<MultipartFile> files;
}