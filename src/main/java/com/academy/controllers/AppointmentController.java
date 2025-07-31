package com.academy.controllers;

import com.academy.dtos.SlotDTO;
import com.academy.dtos.appointment.AppointmentCardDTO;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.dtos.appointment.ConfirmAppointmentResponseDTO;
import com.academy.dtos.appointment.review.ReviewRequestDTO;
import com.academy.dtos.appointment.review.ReviewResponseDTO;
import com.academy.services.AppointmentService;
import com.academy.services.SchedulingService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isClientOrProvider(#id, authentication.name)")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO response = appointmentService.createAppointment(dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isClientOrProvider(#id, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@PathVariable Long id, @RequestBody AppointmentRequestDTO appointmentDetails) {
        AppointmentResponseDTO updated = appointmentService.updateAppointment(id, appointmentDetails);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/confirm-appointment/{id}")
        public ResponseEntity<ConfirmAppointmentResponseDTO> confirmAppointment(@PathVariable Long id){
        return appointmentService.confirmAppointment(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isClientOrProvider(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@appointmentSecurity.isClient(#id, authentication.name)")
    @Deprecated
    @PostMapping("/{id}/review")
    public ResponseEntity<AppointmentResponseDTO> createReview(@PathVariable Long id, @RequestBody AppointmentRequestDTO appointmentDetails) {
        return updateAppointment(id, appointmentDetails);
    }

    @PreAuthorize("@appointmentSecurity.isClient(#id, authentication.name)")
    @PatchMapping("/{id}/review")
    public ResponseEntity<ReviewResponseDTO> addReview(@PathVariable Long id, @RequestBody @Valid ReviewRequestDTO reviewRequestDTO) {
        return appointmentService.addReview(id, reviewRequestDTO);
    }

    @PreAuthorize("hasRole('ADMIN') or @appointmentSecurity.isClient(#id, authentication.name)")
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

    @GetMapping("/services/{serviceId}/free-slots")
    public ResponseEntity<List<SlotDTO>> getFreeSlots(@PathVariable Long serviceId) {
        List<SlotDTO> slots = schedulingService.getFreeSlotsForService(serviceId);
        return ResponseEntity.ok(slots);
    }
}