package com.academy.services;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.repositories.AppointmentRepository;
import com.academy.repositories.MemberRepository;

import com.academy.dtos.appointment.AppointmentResponseDTO;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Appointment;
import com.academy.dtos.appointment.AppointmentMapper;

@Service
public class AppointmentService {

    AppointmentRepository appointmentRepository;
    MemberRepository memberRepository;
    AppointmentMapper appointmentMapper;
    

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, MemberRepository memberRepository, AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
        this.memberRepository = memberRepository;
        this.appointmentRepository = appointmentRepository;
        // Constructor for dependency injection
    }

    // Get all appointments for a specific member by their ID
    public List<AppointmentResponseDTO> getAppointmentsByMemberId(Long memberId) {
        if (memberId == null) {
            throw new InvalidArgumentException("Member ID cannot be null");
        }
        if(memberRepository.existsById(memberId)) {
            throw new InvalidArgumentException("Member with ID " + memberId + " does not exist.");
        }

        List<Appointment> appointments = appointmentRepository.findByMember_Id(memberId);
        return appointments.stream()
                .map(appointmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    
}
