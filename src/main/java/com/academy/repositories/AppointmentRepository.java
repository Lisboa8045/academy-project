package com.academy.repositories;

import com.academy.models.appointment.Appointment;

import java.time.LocalDateTime;
import java.util.List;

import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.service.service_provider.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByMember_Id(Long memberId);


    boolean existsByServiceProviderAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndStatusNot(
            ServiceProvider serviceProvider,
            LocalDateTime endDateTime,
            LocalDateTime startDateTime,
            AppointmentStatus canceledStatus
    );

    List<Appointment> findByServiceProvider_Provider_IdAndStartDateTimeBetween(Long providerId, LocalDateTime now,
            LocalDateTime in30Days);
}
