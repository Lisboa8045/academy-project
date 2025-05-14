// AppointmentService.java
package com.academy.services;

import com.academy.models.Appointment;
import com.academy.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

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
        if(appointmentDetails.getMember() != null) appointment.setMember(appointmentDetails.getMember());
        if(appointmentDetails.getServiceProvider() != null) appointment.setServiceProvider(appointmentDetails.getServiceProvider());
        if(appointmentDetails.getRating() != appointment.getRating() )appointment.setRating(appointmentDetails.getRating());
        if(appointmentDetails.getComment() != null) appointment.setComment(appointmentDetails.getComment());

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(int id) {
        appointmentRepository.deleteById(id);
    }

    public void deleteReview(int id){
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setRating(0);
        appointment.setComment(null);
        appointmentRepository.save(appointment);

    }

}
