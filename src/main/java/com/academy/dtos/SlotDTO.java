package com.academy.dtos;

import java.time.LocalDateTime;

public class SlotDTO {
    
    private Long providerId;
    private String providerName;
    private LocalDateTime start;
    private LocalDateTime end;

    public SlotDTO() {}

    public SlotDTO(Long providerId, String providerName, LocalDateTime start, LocalDateTime end) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.start = start;
        this.end = end;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}