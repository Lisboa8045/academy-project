package com.academy.security;

import com.academy.services.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentSecurity {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentSecurity(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public boolean isClientOrProvider(Long appointmentId, String username) {
        return appointmentService.isAppointmentOwnedByClientOrProvider(appointmentId, username);
    }

    public boolean isClient(Long appointmentId, String username) {
        return appointmentService.isAppointmentOwnedByClient(appointmentId, username);
    }
}
