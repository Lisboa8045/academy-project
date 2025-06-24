// AppointmentService.java

package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.appointment.Appointment;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
                .toList();
    }

    public AppointmentResponseDTO getAppointmentById(Long id) {

        return appointmentRepository.findById(id.intValue())

                .map(appointmentMapper::toResponseDTO)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

    }

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {

        String username = authenticationFacade.getUsername();
        Member member = memberService.getMemberByUsername(username);

        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(dto.serviceProviderId());

        boolean hasServePermission = serviceProvider.getPermissions().stream()
                .map(ProviderPermission::getPermission)
                .anyMatch(p -> p == ProviderPermissionEnum.SERVE);

        if (!hasServePermission) {
            throw new IllegalStateException("Service provider não possui permissão SERVE");
        }

        int serviceDurationMinutes = serviceProvider.getService().getDuration();
        LocalDateTime endDateTime = dto.startDateTime().plusMinutes(serviceDurationMinutes);

        Appointment appointment = appointmentMapper.toEntity(dto);

        appointment.setMember(member);
        appointment.setServiceProvider(serviceProvider);
        appointment.setEndDateTime(endDateTime);

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }




    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO appointmentDetails) {

        Appointment appointment = appointmentRepository.findById(id.intValue())

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));


        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(appointmentDetails.serviceProviderId());
        appointment.setServiceProvider(serviceProvider);


        if(appointmentDetails.rating().equals(appointment.getRating()))
            appointment.setRating(appointmentDetails.rating());

        if(appointmentDetails.comment() != null) appointment.setComment(appointmentDetails.comment());

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));

    }

    public void deleteAppointment(Long id) {

        if(!appointmentRepository.existsById(id.intValue())) throw new EntityNotFoundException(Appointment.class, id);

        appointmentRepository.deleteById(id.intValue());

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
        throw new EntityNotFoundException(Appointment.class, providerId);
    }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(slotWindowDays);
        return appointmentRepository.findByServiceProvider_Provider_IdAndStartDateTimeBetween(providerId, now, end);
    }
}