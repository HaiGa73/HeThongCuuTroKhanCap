package com.rescue.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingPersonDto {
    private String targetName;
    private String gender;
    private Integer age;
    private String lastSeenLocation;
    private String identifyingCharacteristics;
    private String contactPerson;
    private String contactPhone;
    private MultipartFile photoFile;

    // 2. Tên trường phụ trợ khớp với form HTML
    private String fullName;
    private Integer estimatedAge;
    private String description;
    private String reporterName;
    private String reporterPhone;

    // Getter ánh xạ tự động
    public String getTargetName() {
        return (targetName != null && !targetName.isBlank()) ? targetName : fullName;
    }

    public String getFullName() {
        return (fullName != null && !fullName.isBlank()) ? fullName : targetName;
    }

    public Integer getAge() {
        return age != null ? age : estimatedAge;
    }

    public Integer getEstimatedAge() {
        return estimatedAge != null ? estimatedAge : age;
    }

    public String getIdentifyingCharacteristics() {
        return (identifyingCharacteristics != null && !identifyingCharacteristics.isBlank()) ? identifyingCharacteristics : description;
    }

    public String getDescription() {
        return (description != null && !description.isBlank()) ? description : identifyingCharacteristics;
    }

    public String getContactPerson() {
        return (contactPerson != null && !contactPerson.isBlank()) ? contactPerson : reporterName;
    }

    public String getReporterName() {
        return (reporterName != null && !reporterName.isBlank()) ? reporterName : contactPerson;
    }

    public String getContactPhone() {
        return (contactPhone != null && !contactPhone.isBlank()) ? contactPhone : reporterPhone;
    }

    public String getReporterPhone() {
        return (reporterPhone != null && !reporterPhone.isBlank()) ? reporterPhone : contactPhone;
    }
}