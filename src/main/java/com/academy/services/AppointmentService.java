// AppointmentService.java

package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Appointment;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final ServiceProviderService serviceProviderService;

    private final AppointmentMapper appointmentMapper;

    private final MemberService memberService;

    private final AuthenticationFacade authenticationFacade;

    @Value("${slot.window.days:30}")
    private int slotWindowDays;

    @Autowired

    public AppointmentService(AppointmentRepository appointmentRepository, ServiceProviderService serviceProviderService, AppointmentMapper appointmentMapper, MemberService memberService, AuthenticationFacade authenticationFacade) {

        this.appointmentRepository = appointmentRepository;

        this.serviceProviderService = serviceProviderService;

        this.appointmentMapper = appointmentMapper;

        this.memberService = memberService;

        this.authenticationFacade = authenticationFacade;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {

        return appointmentRepository.findAll().stream()

                .map(appointmentMapper::toResponseDTO)

                .collect(Collectors.toList());

    }

    public AppointmentResponseDTO getAppointmentById(int id) {

        return appointmentRepository.findById(id)

                .map(appointmentMapper::toResponseDTO)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

    }

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {

        String username = authenticationFacade.getUsername();
        Member member = memberService.getMemberByUsername(username);

        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(dto.serviceProviderId());

        Appointment appointment = appointmentMapper.toEntity(dto);


        appointment.setMember(member);
        appointment.setServiceProvider(serviceProvider);
        appointment.setStartDateTime(dto.startDateTime());
        appointment.setEndDateTime(dto.endDateTime());
        appointment.setStatus(dto.status());

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }



    public AppointmentResponseDTO updateAppointment(int id, AppointmentRequestDTO appointmentDetails) {

        Appointment appointment = appointmentRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

        String username = authenticationFacade.getUsername();
        Member member = memberService.getMemberByUsername(username);

        if(appointmentDetails.serviceProviderId() != null) {
            ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(appointmentDetails.serviceProviderId());
            appointment.setServiceProvider(serviceProvider);
        }

        if(appointmentDetails.rating() != appointment.getRating() )appointment.setRating(appointmentDetails.rating());

        if(appointmentDetails.comment() != null) appointment.setComment(appointmentDetails.comment());

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));

    }

    public void deleteAppointment(int id) {

        if(!appointmentRepository.existsById(id)) throw new EntityNotFoundException(Appointment.class, id);

        appointmentRepository.deleteById(id);

    }

    public void deleteReview(int id){

        Appointment appointment = appointmentRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

        appointment.setRating(null);

        appointment.setComment(null);

        appointmentRepository.save(appointment);

    }

    public List<Appointment> getAppointmentsForProvider(Long providerId) {
    if (providerId == null) {
        throw new IllegalArgumentException("Provider ID cannot be null");
    }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(slotWindowDays);
        return appointmentRepository.findByServiceProvider_Provider_IdAndStartDateTimeBetween(providerId, now, end);
    }
}

