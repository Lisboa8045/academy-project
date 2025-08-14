package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.appointment.AppointmentCalendarDTO;
import com.academy.dtos.appointment.AppointmentCardDTO;
import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.dtos.appointment.ConfirmAppointmentResponseDTO;
import com.academy.dtos.appointment.review.ReviewRequestDTO;
import com.academy.dtos.appointment.review.ReviewResponseDTO;
import com.academy.exceptions.BadRequestException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.TokenExpiredException;
import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.notification.Notification;
import com.academy.models.notification.NotificationTypeEnum;
import com.academy.models.service.service_provider.ProviderPermission;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AppointmentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceProviderService serviceProviderService;
    private final AppointmentMapper appointmentMapper;
    private final MemberService memberService;
    private final AuthenticationFacade authenticationFacade;
    private final ServiceService serviceService;
    private final EmailService emailService;
    private final AppointmentSchedulerService appointmentSchedulerService;
    private final GlobalConfigurationService globalConfigurationService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository
            , ServiceProviderService serviceProviderService,
                              AppointmentMapper appointmentMapper,
                              MemberService memberService,
                              AuthenticationFacade authenticationFacade,
                              EmailService emailService,
                              AppointmentSchedulerService appointmentSchedulerService,
                              GlobalConfigurationService globalConfigurationService,
                              ServiceService serviceService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceProviderService = serviceProviderService;
        this.appointmentMapper = appointmentMapper;
        this.memberService = memberService;
        this.authenticationFacade = authenticationFacade;
        this.serviceService = serviceService;
        this.emailService = emailService;
        this.appointmentSchedulerService = appointmentSchedulerService;
        this.globalConfigurationService = globalConfigurationService;
        this.notificationService = notificationService;
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
        appointment.setPrice(price - (price * service.getDiscount() / 100));

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

        if (appointmentDetails.comment() != null) appointment.setComment(appointmentDetails.comment());

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));

    }


    public void deleteReview(Long id) {

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

    public List<AppointmentCalendarDTO> getAppointmentsForAuthenticatedServiceProviderCalendar() {

        List<Appointment> appointmentList = appointmentRepository
                .findAllByServiceProviderProviderUsernameAndStatusIsNot(authenticationFacade.getUsername(), AppointmentStatus.CANCELLED);
        return appointmentList.stream().map(appointmentMapper::toAppointmentCalendarDTO).toList();
    }


    public List<Appointment> getAppointmentsForServiceProvider(Long serviceProviderId) {
        return appointmentRepository.findByServiceProviderId(serviceProviderId).stream().filter(
                appointment -> !appointment.getStatus().equals(AppointmentStatus.CANCELLED)
        ).toList();
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        if(appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new BadRequestException("Appointment can't be canceled with status " + appointment.getStatus());
        String loggedMemberUsername = authenticationFacade.getUsername();
        if(appointment.getServiceProvider().getProvider().getUsername().equals(loggedMemberUsername)){
            if(AppointmentStatus.PENDING.equals(appointment.getStatus()))
                throw new BadRequestException("Pending payment appointment can't be cancelled");
            emailService.sendCancelAppointmentClientEmail(appointment);
            sendNotificationToClientCancelledAppointment(appointment);
        }
        else{
            emailService.sendCancelAppointmentProviderEmail(appointment);
            sendNotificationToProviderCancelledAppointment(appointment);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public ResponseEntity<ReviewResponseDTO> addReview(Long appointmentId, ReviewRequestDTO request) {
        Appointment appointment = getAppointmentEntityById(appointmentId);

        appointment.setRating(request.rating());
        appointment.setComment(request.comment());
        appointment = appointmentRepository.save(appointment);

        serviceProviderService.updateRating(appointment.getServiceProvider().getId());
        serviceService.updateRating(appointment.getServiceProvider().getService().getId());
        memberService.updateMemberRating(appointment.getServiceProvider().getProvider().getId());

        sendNotificationToProviderReviewAdded(appointment);
    return ResponseEntity.ok(new ReviewResponseDTO("Review added successfully"));
    }

    public ResponseEntity<ConfirmAppointmentResponseDTO> confirmAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        if (AppointmentStatus.CANCELLED.equals(appointment.getStatus()))
            throw new TokenExpiredException("Time to confirm appointment has passed" + appointment.getStatus());
        if (!AppointmentStatus.PENDING.equals(appointment.getStatus()))
            throw new BadRequestException("Appointment can't be confirmed with status " + appointment.getStatus());

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        sendNotificationToProviderConfirmedAppointment(appointment);
        return ResponseEntity.ok(new ConfirmAppointmentResponseDTO("Appointment confirmed successfully"));
    }

    private void sendNotificationToProviderReviewAdded(Appointment appointment) {
        Notification notification = new Notification();
        notification.setNotificationTypeEnum(NotificationTypeEnum.APPOINTMENT_REVIEW_ADDED);
        notification.setTitle(appointment.getServiceProvider().getService().getName());
        notification.setBody("A review has been added");
        notification.setMember(appointment.getServiceProvider().getProvider());
        notificationService.createNotification(notification);
    }

    private void sendNotificationToProviderConfirmedAppointment(Appointment appointment) {
        Notification notification = new Notification();
        notification.setNotificationTypeEnum(NotificationTypeEnum.APPOINTMENT_CONFIRMED);
        notification.setTitle(appointment.getServiceProvider().getService().getName());
        notification.setBody("Appointment with "
                + appointment.getMember().getUsername()
                + " at " + formatDate(appointment.getStartDateTime()) + " has been confirmed.");
        notification.setMember(appointment.getServiceProvider().getProvider());
        notificationService.createNotification(notification);
    }
    private void sendNotificationToProviderCancelledAppointment(Appointment appointment) {
        Notification notification = new Notification();
        notification.setNotificationTypeEnum(NotificationTypeEnum.APPOINTMENT_CANCELLED);
        notification.setTitle(appointment.getServiceProvider().getService().getName());
        notification.setBody("Appointment with "
                + appointment.getMember().getUsername()
                + " at " + formatDate(appointment.getStartDateTime()) + " has been cancelled by the client.");
        notification.setMember(appointment.getServiceProvider().getProvider());
        notificationService.createNotification(notification);
    }

    private void sendNotificationToClientCancelledAppointment(Appointment appointment) {
        Notification notification = new Notification();
        notification.setNotificationTypeEnum(NotificationTypeEnum.APPOINTMENT_CANCELLED);
        notification.setTitle(appointment.getServiceProvider().getService().getName());
        notification.setBody("Appointment for "
                + appointment.getServiceProvider().getService().getName()
                + " at " + formatDate(appointment.getStartDateTime()) + " has been cancelled by the provider");
        notification.setMember(appointment.getMember());
        notificationService.createNotification(notification);
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

        // Format the datetime
        return dateTime.format(formatter);

    }

    public List<Member> getAllMembersThatHaveAppointmentsInAServiceProvider(Long serviceProviderId){
        return appointmentRepository.findDistinctMembersByServiceProviderId(serviceProviderId);
    }

    public List<AppointmentCardDTO> getAppointmentsForService(Long id, String dateOrder) {
        Sort sort = dateOrder.equalsIgnoreCase("desc") ? Sort.by("startDateTime").descending() : Sort.by("startDateTime").ascending();
        authenticationFacade.getUsername();
        List<Appointment> appointmentList = appointmentRepository
                .findByServiceId(id, sort);

        return appointmentList.stream().map(appointmentMapper::toAppointmentCardDTO).toList();
    }

}
