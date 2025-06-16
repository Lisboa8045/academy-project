package com.academy.repositories;

import com.academy.models.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);

    boolean existsByConfirmationToken(String encodedToken);

    Optional<Member> findByConfirmationToken(String confirmationToken);
}
