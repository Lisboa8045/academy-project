package com.academy.repositories;

import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);

    boolean existsByConfirmationToken(String encodedToken);

    Optional<Member> findByConfirmationToken(String confirmationToken);

    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.tokenExpiry IS NOT NULL AND m.tokenExpiry < :now")
    List<Member> findExpiredForDeletion(
            @Param("status") MemberStatusEnum status,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT s FROM Service s WHERE s.owner.id = :memberId")
    List<Service> findOwnerServices(@Param("memberId") Long memberId);
}
