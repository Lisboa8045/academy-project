package com.academy.services;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.member.Member;
import com.academy.models.token.MemberToken;
import com.academy.models.token.TokenTypeEnum;
import com.academy.repositories.MemberTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberTokenService {

    private final MemberTokenRepository memberTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberTokenService(MemberTokenRepository memberTokenRepository,
                              PasswordEncoder passwordEncoder) {
        this.memberTokenRepository = memberTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberToken createToken(Member member, LocalDateTime expirationDate, TokenTypeEnum tokenType) {
        String rawToken = generateUniqueConfirmationToken();
        String encodedToken = passwordEncoder.encode(rawToken);

        MemberToken token = new MemberToken();
        token.setRawValue(rawToken);
        token.setEncodedValue(encodedToken);
        token.setMember(member);
        token.setExpirationDate(expirationDate);
        token.setTokenType(tokenType);
        return memberTokenRepository.save(token);
    }

    public MemberToken getTokenByRawValue(String rawValue, TokenTypeEnum expectedType) {
        return memberTokenRepository
                .findByRawValueAndTokenType(rawValue, expectedType).orElseThrow(() -> new EntityNotFoundException(MemberToken.class, rawValue));
    }

    public List<MemberToken> getValidTokensByMemberAndType(Member member, TokenTypeEnum tokenType) {
        return memberTokenRepository.findValidTokensByMemberAndType(member, tokenType);
    }

    private String generateUniqueConfirmationToken() {
        String rawToken;
        Optional<MemberToken> optionalToken;

        do {
            rawToken = generateEncodedToken();
            String finalRawToken = rawToken;
            optionalToken = memberTokenRepository.findAll().stream()
                    .filter(e -> e.getRawValue() != null &&
                            passwordEncoder.matches(finalRawToken, e.getRawValue()))
                    .findFirst();
        } while (optionalToken.isPresent());

        return rawToken;
    }

    private String generateEncodedToken(){
        return UUID.randomUUID().toString();
    }

    @Transactional
    public void deleteAllByTokenTypeForMember(Member member, TokenTypeEnum tokenType) {
        memberTokenRepository.deleteByMemberAndTokenType(member, tokenType);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void deleteOldExpiredTokens() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        memberTokenRepository.deleteAllExpiredTokensBefore(oneWeekAgo);
    }
}
