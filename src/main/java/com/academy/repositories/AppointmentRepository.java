package com.academy.repositories;

import com.academy.models.Appointment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByMember_Id(Long memberId);
}
