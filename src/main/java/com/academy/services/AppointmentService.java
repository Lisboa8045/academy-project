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
import com.academy.repositories.MemberRepository;
import com.academy.repositories.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final AppointmentMapper appointmentMapper;

    private final MemberRepository memberRepository;

    @Autowired

    public AppointmentService(AppointmentRepository appointmentRepository, ServiceProviderRepository serviceProviderRepository, AppointmentMapper appointmentMapper, MemberRepository memberRepository) {

        this.appointmentRepository = appointmentRepository;

        this.serviceProviderRepository = serviceProviderRepository;

        this.appointmentMapper = appointmentMapper;

        this.memberRepository = memberRepository;

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

        ServiceProvider serviceProvider = serviceProviderRepository.findById(dto.serviceProviderId())

                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, dto.serviceProviderId()));

        Member member = memberRepository.findById(dto.memberId())

                .orElseThrow(() -> new EntityNotFoundException(Member.class, dto.memberId()));

        Appointment appointment = appointmentMapper.toEntity(dto);

        appointment.setServiceProvider(serviceProvider);

        appointment.setMember(member);

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));

    }

    
    public AppointmentResponseDTO updateAppointment(int id, AppointmentRequestDTO appointmentDetails) {

        Appointment appointment = appointmentRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, id));

        if(appointmentDetails.memberId() != null){

            Member member = memberRepository.findById(appointmentDetails.memberId())

                    .orElseThrow(()-> new EntityNotFoundException(Member.class, appointmentDetails.memberId()));

            appointment.setMember(member);

        }

        if(appointmentDetails.serviceProviderId() != null) {

            ServiceProvider serviceProvider = serviceProviderRepository.findById(appointmentDetails.serviceProviderId())

                    .orElseThrow(()-> new EntityNotFoundException(ServiceProvider.class, appointmentDetails.serviceProviderId()));

            appointment.setServiceProvider(serviceProvider);;

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

