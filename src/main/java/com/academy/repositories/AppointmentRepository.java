package com.academy.repositories;

import com.academy.models.Appointment;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByMember_Id(Long memberId);
    Page<Appointment> findByMember_Username(String username, Pageable pageable);


    //Collection<Appointment> findByProvider_Username(String );
}
