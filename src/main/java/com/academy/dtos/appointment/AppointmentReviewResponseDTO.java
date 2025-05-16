package com.academy.dtos.appointment;

import lombok.Data;

@Data
public class AppointmentReviewResponseDTO {
        private Long id;
        private Integer rating;
        private String comment;
        private String memberUsername;

}

