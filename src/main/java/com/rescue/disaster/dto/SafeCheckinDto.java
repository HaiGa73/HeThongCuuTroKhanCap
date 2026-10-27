package com.rescue.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafeCheckinDto {
    private String fullName;
    private String phoneNumber;
    private String idCardNumber;
    private String originAddress;
    private String temporaryAddress;
    private String healthStatus;
    private Long shelterId;

    private String identityCard;
    private String homeAddress;
    private String message;

    public String getIdCardNumber() {
        return (idCardNumber != null && !idCardNumber.isBlank()) ? idCardNumber : identityCard;
    }

    public String getIdentityCard() {
        return (identityCard != null && !identityCard.isBlank()) ? identityCard : idCardNumber;
    }

    public String getOriginAddress() {
        return (originAddress != null && !originAddress.isBlank()) ? originAddress : homeAddress;
    }

    public String getHomeAddress() {
        return (homeAddress != null && !homeAddress.isBlank()) ? homeAddress : originAddress;
    }

    public String getTemporaryAddress() {
        return temporaryAddress != null ? temporaryAddress : homeAddress;
    }
}