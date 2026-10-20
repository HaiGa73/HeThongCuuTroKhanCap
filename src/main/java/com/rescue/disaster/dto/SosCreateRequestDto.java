package com.rescue.disaster.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SosCreateRequestDto {
    private String senderName;
    private String phoneNumber;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer numberOfPeople;
    private Boolean isUrgent;
    private String description;
    private List<MultipartFile> files;
}