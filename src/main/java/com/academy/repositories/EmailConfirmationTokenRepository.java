package com.academy.repositories;

import com.academy.models.member.Member;
import com.academy.models.token.EmailConfirmationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {
    List<EmailConfirmationToken> findByMemberOrderByCreatedAtDesc(Member member);

    @Query("SELECT t FROM EmailConfirmationToken t WHERE t.member = :member AND t.expirationDate > CURRENT_TIMESTAMP")
    List<EmailConfirmationToken> findValidTokensByMember(Member member);

    Optional<EmailConfirmationToken> findByRawValue(String rawToken);


    @Transactional
    void deleteByMember(Member member);
}
