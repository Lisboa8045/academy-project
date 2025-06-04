// main/java/com/academy/repositories/AvailabilityRepository.java

package com.academy.repositories;

import com.academy.models.Availability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByMember_Id(Long memberId);


    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Availability a " +
           "WHERE a.member.id = :memberId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND ((a.startDateTime < :endDateTime) AND (a.endDateTime > :startDateTime))")
    boolean existsByMember_IdAndDayOfWeekAndTimeOverlap(
        @Param("memberId") long memberId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);

}