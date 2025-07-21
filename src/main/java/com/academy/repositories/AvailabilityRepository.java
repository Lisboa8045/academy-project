package com.academy.repositories;

import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.models.availability.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Query("SELECT a FROM Availability a WHERE a.datesString LIKE CONCAT('%', :dateStr, '%')")
    List<Availability> findByDate(@Param("dateStr") String dateStr);

    List<Availability> findByStartTimeAndEndTime(LocalTime startDateTime, LocalTime endDateTime);

    //List<Availability> findByMember_Id(Long memberId);

    //List<Availability> findByMember_IdAndIsExceptionFalse(Long memberId);

    //boolean existsByMember_IdAndIsExceptionFalse(Long memberId);

    //@ModifyingAQ
    //@Query("DELETE FROM Availability a WHERE a.member.id = :memberId AND a.isException = false")
    //void deleteByMember_IdAndIsExceptionFalse(@Param("memberId") Long memberId);

    /*@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.member.id = :memberId " +
            "AND a.dayOfWeek = :dayOfWeek " +
            "AND ((a.startDateTime < :endDateTime) AND (a.endDateTime > :startDateTime))")
    boolean existsByMember_IdAndDayOfWeekAndTimeOverlap(
            @Param("memberId") Long memberId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    List<Availability> findByMember_IdAndStartDateTimeBetween(Long memberId, LocalDateTime start, LocalDateTime end);
    AQ
     */

    //Collection<AvailabilityResponseDTO> findByMember_IdAndIsExceptionTrue(Long memberId);
}