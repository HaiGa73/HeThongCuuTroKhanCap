package com.rescue.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlertDto {
    private String title;
    
    private String content;
    private Integer regionId;
    private String severity;

    private String message;
    private String level;
    private String alertLevel;
    private String affectedArea;
    private String area;
    private LocalDateTime createdAt;

    public String getContent() {
        if (content != null && !content.isBlank()) return content;
        return message;
    }

    public void setContent(String content) {
        this.content = content;
        this.message = content;
    }

    public String getMessage() {
        if (message != null && !message.isBlank()) return message;
        return content;
    }

    public void setMessage(String message) {
        this.message = message;
        this.content = message;
    }

    // Ánh xạ mức độ khẩn cấp (severity / level / alertLevel)
    public String getSeverity() {
        if (severity != null && !severity.isBlank()) return severity;
        if (level != null && !level.isBlank()) return level;
        return alertLevel;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
        this.level = severity;
        this.alertLevel = severity;
    }

    public String getLevel() {
        return getSeverity();
    }

    public void setLevel(String level) {
        setSeverity(level);
    }

    public String getAlertLevel() {
        return getSeverity();
    }

    public void setAlertLevel(String alertLevel) {
        setSeverity(alertLevel);
    }

    // Ánh xạ vùng ảnh hưởng
    public String getAffectedArea() {
        return (affectedArea != null && !affectedArea.isBlank()) ? affectedArea : area;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
        this.area = affectedArea;
    }

    public String getArea() {
        return getAffectedArea();
    }

    public void setArea(String area) {
        setAffectedArea(area);
    }
}