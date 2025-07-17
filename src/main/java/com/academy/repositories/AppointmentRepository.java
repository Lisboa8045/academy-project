package com.academy.repositories;
import com.academy.models.appointment.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Appointment> findByServiceProvider_Provider_IdAndStartDateTimeBetween(Long providerId, LocalDateTime now,
            LocalDateTime in30Days);

    List<Appointment> findByServiceProviderId(Long serviceProviderId);

    @Query("SELECT AVG(ap.rating) FROM Appointment ap WHERE ap.serviceProvider.id = :serviceProviderId")
    Double findAverageRatingByServiceProvider_Id(@Param("serviceProviderId") Long serviceProviderId);
}
