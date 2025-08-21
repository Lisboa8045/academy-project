package com.academy.repositories;

import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.token.MemberToken;
import com.academy.models.token.TokenTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberTokenRepository extends JpaRepository<MemberToken, Long> {

    @Query("SELECT t FROM MemberToken t " +
            "WHERE t.member = :member " +
            "AND t.tokenType = :tokenType " +
            "AND t.expirationDate > CURRENT_TIMESTAMP")
    List<MemberToken> findValidTokensByMemberAndType(Member member, TokenTypeEnum tokenType);

    @Query("SELECT t.member FROM MemberToken t " +
            "WHERE t.tokenType = com.academy.models.token.TokenTypeEnum.ACCOUNT_DELETION " +
            "AND t.member.status = :status " +
            "AND t.expirationDate < :now")
    List<Member> findMembersWithExpiredRevertDeletionTokens(
            @Param("status") MemberStatusEnum status,
            @Param("now") LocalDateTime now
    );

    Optional<MemberToken> findByRawValueAndTokenType(String rawValue, TokenTypeEnum tokenType);

    void deleteByMemberAndTokenType(Member member, TokenTypeEnum tokenType);
}
