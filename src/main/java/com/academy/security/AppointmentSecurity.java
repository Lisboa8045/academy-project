package com.academy.security;

import com.academy.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentSecurity {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentSecurity(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public boolean isClientOrProvider(Long appointmentId, String username) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> {
                    String clientUsername = appointment.getMember().getUsername();
                    String providerUsername = appointment.getServiceProvider().getProvider().getUsername();
                    return username.equals(clientUsername) || username.equals(providerUsername);
                })
                .orElse(false);
    }

    public boolean isClient(Long appointmentId, String username) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> username.equals(appointment.getMember().getUsername()))
                .orElse(false);
    }
}
