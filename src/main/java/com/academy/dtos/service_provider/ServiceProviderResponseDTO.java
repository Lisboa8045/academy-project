package com.academy.dtos.service_provider;

import com.academy.dtos.appointment.AppointmentReviewResponseDTO;
import lombok.Data;

import java.util.List;
@Data
public class ServiceProviderResponseDTO {

    private long id;
    private String memberName;
    private long serviceId;
    private List<AppointmentReviewResponseDTO> appointmentReviewList;
    private String permission;
}
