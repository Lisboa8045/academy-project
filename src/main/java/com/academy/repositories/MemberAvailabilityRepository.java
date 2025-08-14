package com.academy.repositories;

import com.academy.models.availability.MemberAvailability;
import com.academy.models.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAvailabilityRepository extends JpaRepository<MemberAvailability, Long> {
    List<MemberAvailability> findByMemberId(Long memberId);

    int deleteByMemberIdAndDatesStringContaining(Long memberId, String dateStrWithCommas);

    Long member(Member member);
}
