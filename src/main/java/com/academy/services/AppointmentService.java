package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.appointment.*;
import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.appointment.review.ReviewRequestDTO;
import com.academy.dtos.appointment.review.ReviewResponseDTO;
import com.academy.exceptions.BadRequestException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.TokenExpiredException;
import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.academy.utils.Utils.formatHours;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceProviderService serviceProviderService;
    private final AppointmentMapper appointmentMapper;
    private final MemberService memberService;
    private final AuthenticationFacade authenticationFacade;
    private final EmailService emailService;
    private final AppointmentSchedulerService appointmentSchedulerService;
    private final GlobalConfigurationService globalConfigurationService;

    @Value("${slot.window.days:30}")
    private int slotWindowDays;

    @Autowired

    public AppointmentService(AppointmentRepository appointmentRepository
            , ServiceProviderService serviceProviderService,
                              AppointmentMapper appointmentMapper,
                              MemberService memberService,
                              AuthenticationFacade authenticationFacade,
                              EmailService emailService,
                              AppointmentSchedulerService appointmentSchedulerService,
                              GlobalConfigurationService globalConfigurationService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceProviderService = serviceProviderService;
        this.appointmentMapper = appointmentMapper;
        this.memberService = memberService;
        this.authenticationFacade = authenticationFacade;
        this.emailService = emailService;
        this.appointmentSchedulerService = appointmentSchedulerService;
        this.globalConfigurationService = globalConfigurationService;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponseDTO)
                .toList();
    }
    public Appointment getAppointmentEntityById(long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));
    }
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return appointmentMapper.toResponseDTO(getAppointmentEntityById(id));
    }

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        // Validate date is not in the past
        if (dto.startDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cant schedule in the past");
        }

        String username = authenticationFacade.getUsername();
        Member member = memberService.getMemberByUsername(username);

        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(dto.serviceProviderId());

        // Validate SERVE permission
        boolean hasServePermission = serviceProvider.getPermissions().stream()
                .map(ProviderPermission::getPermission)
                .anyMatch(p -> p == ProviderPermissionEnum.SERVE);
        if (!hasServePermission) {
            throw new IllegalStateException("Service provider doesn't have SERVE permission");
        }
        com.academy.models.service.Service service = serviceProvider.getService();

        // Calculate end time
        int serviceDurationMinutes = service.getDuration();
        LocalDateTime endDateTime = dto.startDateTime().plusMinutes(serviceDurationMinutes);

        // Check for conflicts
        List<Appointment> conflictingAppointments = appointmentRepository
                .findConflictingAppointments(
                        serviceProvider.getId(),
                        dto.startDateTime(),
                        endDateTime
                );
        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalStateException("An appointment already exists for this time");
        }

        // Create and save appointment
        double price = service.getPrice();
        Appointment appointment = appointmentMapper.toEntity(dto);
        appointment.setMember(member);
        appointment.setServiceProvider(serviceProvider);
        appointment.setEndDateTime(endDateTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPrice(price - ( price * service.getDiscount()/100));

        appointment = appointmentRepository.save(appointment);

        emailService.sendAppointmentConfirmationEmail(appointment);

        appointmentSchedulerService.scheduleAutoCancel(appointment, Integer.parseInt(globalConfigurationService.getConfigValue("confirm_appointment_expiry_minutes")));

        return appointmentMapper.toResponseDTO(appointment);
    }

    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO appointmentDetails) {

        Appointment appointment = appointmentRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));


        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(appointmentDetails.serviceProviderId());
        appointment.setServiceProvider(serviceProvider);


        if(appointmentDetails.rating().equals(appointment.getRating()))
            appointment.setRating(appointmentDetails.rating());

        if(appointmentDetails.comment() != null) appointment.setComment(appointmentDetails.comment());

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));

    }


    public void deleteReview(Long id){

        Appointment appointment = appointmentRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

        appointment.setRating(null);

        appointment.setComment(null);

        appointmentRepository.save(appointment);

    }

    public List<AppointmentCardDTO> getAppointmentsForAuthenticatedMember(String dateOrder) {
        Sort sort = dateOrder.equalsIgnoreCase("desc") ? Sort.by("startDateTime").descending() : Sort.by("startDateTime").ascending();

        List<Appointment> appointmentList = appointmentRepository
                .findByMember_Username(authenticationFacade.getUsername(), sort);

        return appointmentList.stream().map(appointmentMapper::toAppointmentCardDTO).toList();

    }

/*
    public List<AppointmentResponseDTO> getAppointmentsForAuthenticatedProvider() {
        return appointmentRepository.findByProvider_Username(authenticationFacade.getUsername()).stream()
                .map(appointmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

 */

    public List<Appointment> getAppointmentsForProvider(Long providerId) {
    if (providerId == null) {
        throw new EntityNotFoundException(Appointment.class, providerId);
    }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(slotWindowDays);
        return appointmentRepository.findByServiceProvider_Provider_IdAndStartDateTimeBetween(providerId, now, end);
    }

    public List<Appointment> getAppointmentsForServiceProvider(Long serviceProviderId) {
        return appointmentRepository.findByServiceProviderId(serviceProviderId).stream().filter(
                appointment -> !appointment.getStatus().equals(AppointmentStatus.CANCELLED)
        ).toList();
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        if(appointment.getStatus() != AppointmentStatus.PENDING)
            throw new BadRequestException("Appointment can't be canceled with status " + appointment.getStatus());

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public ResponseEntity<ReviewResponseDTO> addReview(Long appointmentId, ReviewRequestDTO request) {
        Appointment appointment = getAppointmentEntityById(appointmentId);

        appointment.setRating(request.rating());
        appointment.setComment(request.comment());
        appointmentRepository.save(appointment);

        return ResponseEntity.ok(new ReviewResponseDTO("Review added successfully"));
    }

    public ResponseEntity<ConfirmAppointmentResponseDTO> confirmAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        if(AppointmentStatus.CANCELLED.equals(appointment.getStatus()))
            throw new TokenExpiredException("Time to confirm appointment has passed" + appointment.getStatus());
        if(!AppointmentStatus.PENDING.equals(appointment.getStatus()))
            throw new BadRequestException("Appointment can't be confirmed with status " + appointment.getStatus());

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new ConfirmAppointmentResponseDTO("Appointment confirmed successfully"));
    }
}
