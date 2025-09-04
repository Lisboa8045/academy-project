package com.academy.services;

import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.academy.services.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentCleanupService {
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public void cancelAppointmentsForService(Long serviceId) {
        List<Appointment> appointments = appointmentRepository.findAllByServiceId(serviceId);
        for(Appointment appointment : appointments) {
            if(!AppointmentStatus.FINISHED.equals(appointment.getStatus())
                    && !AppointmentStatus.CANCELLED.equals(appointment.getStatus())) {
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);
                emailService.sendCancelAppointmentClientEmail(appointment);
            }
        }
    }
}