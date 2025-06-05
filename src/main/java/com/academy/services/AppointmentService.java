// AppointmentService.java

package com.academy.services;

import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Appointment;
import com.academy.models.Member;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final ServiceProviderService serviceProviderService;

    private final AppointmentMapper appointmentMapper;

    private final MemberService memberService;

    @Autowired

    public AppointmentService(AppointmentRepository appointmentRepository, ServiceProviderService serviceProviderService, AppointmentMapper appointmentMapper, MemberService memberService) {

        this.appointmentRepository = appointmentRepository;

        this.serviceProviderService = serviceProviderService;

        this.appointmentMapper = appointmentMapper;

        this.memberService = memberService;

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

        ServiceProvider serviceProvider = serviceProviderService.getServiceProviderEntityById(dto.serviceProviderId());

        Member member = memberService.getMemberEntityById(dto.memberId());
        Appointment appointment = appointmentMapper.toEntity(dto);

        appointment.setServiceProvider(serviceProvider);

        appointment.setMember(member);

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));

    }

    
    public AppointmentResponseDTO updateAppointment(int id, AppointmentRequestDTO appointmentDetails) {

        Appointment appointment = appointmentRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

        if(appointmentDetails.memberId() != null){
            Member member = memberService.getMemberEntityById(appointmentDetails.memberId());
            appointment.setMember(member);

        }

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

}

