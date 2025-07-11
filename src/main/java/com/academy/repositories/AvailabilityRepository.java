package com.academy.repositories;

import com.academy.models.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByMember_Id(Long memberId);

    List<Availability> findByMember_IdAndIsDefaultTrue(Long memberId);

    boolean existsByMember_IdAndIsDefaultTrue(Long memberId);

    @Modifying
    @Query("DELETE FROM Availability a WHERE a.member.id = :memberId AND a.isDefault = true")
    void deleteByMember_IdAndIsDefaultTrue(@Param("memberId") Long memberId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
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
}