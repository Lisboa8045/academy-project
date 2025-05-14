package com.academy.dtos.tag;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TagResponseDTO {
    private Long id;
    private String name;
    private Boolean isCustom;
    private List<Long> serviceIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}