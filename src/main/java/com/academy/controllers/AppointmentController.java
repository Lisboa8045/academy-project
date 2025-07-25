// AppointmentController.java
package com.academy.controllers;

import com.academy.dtos.appointment.AppointmentCardDTO;
import com.academy.dtos.SlotDTO;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.dtos.appointment.ConfirmAppointmentResponseDTO;
import com.academy.dtos.appointment.review.ReviewRequestDTO;
import com.academy.dtos.appointment.review.ReviewResponseDTO;
import com.academy.services.AppointmentService;
import com.academy.services.SchedulingService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO response = appointmentService.createAppointment(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@PathVariable Long id, @RequestBody AppointmentRequestDTO appointmentDetails) {
        AppointmentResponseDTO updated = appointmentService.updateAppointment(id, appointmentDetails);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/confirm-appointment/{id}")
        public ResponseEntity<ConfirmAppointmentResponseDTO> confirmAppointment(@PathVariable Long id){
        return appointmentService.confirmAppointment(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @Deprecated
    @PostMapping("/{id}/review")
    public ResponseEntity<AppointmentResponseDTO> createReview(@PathVariable Long id, @RequestBody AppointmentRequestDTO appointmentDetails) {
        return updateAppointment(id, appointmentDetails);
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ReviewResponseDTO> addReview(@PathVariable Long id, @RequestBody @Valid ReviewRequestDTO reviewRequestDTO) {
        return appointmentService.addReview(id, reviewRequestDTO);
    }

    @DeleteMapping("/{id}/review")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        appointmentService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member")
    public ResponseEntity<List<AppointmentCardDTO>> getAppointmentsForAuthenticatedMember(
            @RequestParam(defaultValue = "asc") String dateOrder
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForAuthenticatedMember(dateOrder));
    }
/*
    @GetMapping("/provider")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsForAuthenticatedProvider(){
        return ResponseEntity.ok(appointmentService.getAppointmentsForAuthenticatedProvider());
    }

 */

    @GetMapping("/services/{serviceId}/free-slots")
    public ResponseEntity<List<SlotDTO>> getFreeSlots(@PathVariable Long serviceId) {
        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(serviceId);
        return ResponseEntity.ok(slots);
    }
}