// main/java/com/academy/repositories/AvailabilityRepository.java

package com.academy.repositories;

import com.academy.models.Availability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByMember_Id(Long memberId);
    
    Optional<Availability> findByMember_IdAndDayOfWeekAndStartDateTimeAndEndDateTime(
    Long memberId, DayOfWeek dayOfWeek, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Availability> findByMember_IdAndStartDateTimeBetween(Long providerId, LocalDateTime now,
            LocalDateTime in30Days);
}