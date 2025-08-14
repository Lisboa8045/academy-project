package com.academy.repositories;

import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByServiceProviderId(Long serviceProviderProviderId);
    List<Appointment> findByMember_Id(Long memberId);
    List<Appointment> findByMember_Username(String username, Sort sort);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.serviceProvider.id = :providerId " +
            "AND ((a.startDateTime < :end AND a.endDateTime > :start) " +
            "OR (a.startDateTime = :start AND a.endDateTime = :end))")
    List<Appointment> findConflictingAppointments(
            @Param("providerId") Long providerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Transactional
    @Modifying
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id AND a.status = 'PENDING'")
    void cancelIfStillPending(Long id, AppointmentStatus status);

    List<Appointment> findByServiceProvider_Provider_IdAndStartDateTimeBetween(Long providerId, LocalDateTime now,
            LocalDateTime in30Days);

    List<Appointment> findByServiceProviderId(Long serviceProviderId);

        @Query("SELECT DISTINCT a.member " +
                "FROM Appointment a " +
                "WHERE a.serviceProvider.id = :serviceProviderId")
        List<Member> findDistinctMembersByServiceProviderId(Long serviceProviderId);
}
