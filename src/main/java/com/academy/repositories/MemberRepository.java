package com.academy.repositories;

import com.academy.models.member.Member;
import com.academy.models.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);

    @Query("SELECT s FROM Service s WHERE s.owner.id = :memberId")
    List<Service> findOwnerServices(@Param("memberId") Long memberId);

    List<Member> searchMemberByUsernameContainsIgnoreCaseAndRoleNameAndEnabled(String username, String roleName, boolean enabled);

    @Modifying
    @Query("UPDATE Member m SET m.deletionTokensSentToday = 0")
    void resetDeletionTokensSentToday();
}
