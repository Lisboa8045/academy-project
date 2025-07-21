package com.academy.repositories;

import com.academy.models.availability.Availability;
import com.academy.models.availability.MemberAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberAvailabilityRepository extends JpaRepository<MemberAvailability, Long> {
    List<MemberAvailability> findByMemberId(Long memberId);

    @Query("SELECT ma FROM MemberAvailability ma WHERE ma.member.id = :memberId AND " +
            "CONCAT(',', ma.datesString, ',') LIKE %:dateStrWithCommas%")
    List<MemberAvailability> findByMemberIdAndDate(@Param("memberId") Long memberId,
                                                   @Param("dateStrWithCommas") String dateStrWithCommas);

}
