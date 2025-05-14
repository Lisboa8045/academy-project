// AppointmentService.java
package com.academy.services;

import com.academy.models.Appointment;
import com.academy.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(int id) {
        return appointmentRepository.findById(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(int id, Appointment appointmentDetails) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setMember(appointmentDetails.getMember());
        appointment.setServiceProvider(appointmentDetails.getServiceProvider());
        appointment.setRating(appointmentDetails.getRating());
        appointment.setComment(appointmentDetails.getComment());
        appointment.setUpdatedAt(appointmentDetails.getUpdatedAt());

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(int id) {
        appointmentRepository.deleteById(id);
    }
}
