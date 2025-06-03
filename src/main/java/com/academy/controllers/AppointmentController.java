// AppointmentController.java
package com.academy.controllers;

import com.academy.dtos.SlotDTO;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.services.AppointmentService;
import com.academy.services.SchedulingService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final SchedulingService schedulingService;

    public AppointmentController(AppointmentService appointmentService, SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable int id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO response = appointmentService.createAppointment(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@PathVariable int id, @RequestBody AppointmentRequestDTO appointmentDetails) {
        AppointmentResponseDTO updated = appointmentService.updateAppointment(id, appointmentDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable int id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<AppointmentResponseDTO> createReview(@PathVariable int id, @RequestBody AppointmentRequestDTO appointmentDetails) {
        return updateAppointment(id, appointmentDetails);
    }

    @DeleteMapping("/{id}/review")
    public ResponseEntity<Void> deleteReview(@PathVariable int id) {
        appointmentService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/{serviceId}/free-slots")
    public ResponseEntity<List<SlotDTO>> getFreeSlots(@PathVariable Long serviceId) {
        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(serviceId);
        System.out.println("[DEBUG] Number of free slots found: " + slots.size());
        return ResponseEntity.ok(slots);
    }
}