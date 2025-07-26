package com.academy.services;

import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.models.token.EmailConfirmationToken;
import com.academy.repositories.EmailConfirmationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailConfirmationTokenService {

    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public EmailConfirmationTokenService(EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                                         PasswordEncoder passwordEncoder) {
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EmailConfirmationToken createEmailConfirmationToken(Member member, LocalDateTime expirationDate){
        String rawToken = generateUniqueConfirmationToken();
        String encodedToken = passwordEncoder.encode(rawToken);

        EmailConfirmationToken emailConfirmationToken = new EmailConfirmationToken();
        emailConfirmationToken.setRawValue(rawToken);
        emailConfirmationToken.setEncondedValue(encodedToken);
        emailConfirmationToken.setMember(member);
        emailConfirmationToken.setExpirationDate(expirationDate);
        return emailConfirmationTokenRepository.save(emailConfirmationToken);
    }

    public EmailConfirmationToken getTokenByRawValue(String rawValue) {
        EmailConfirmationToken token = emailConfirmationTokenRepository.findByRawValue(rawValue)
                .orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, rawValue));
        return token;
    }

    public List<EmailConfirmationToken> getValidTokensByMember(Member member) {
        return emailConfirmationTokenRepository.findValidTokensByMember(member);
    }

    private String generateUniqueConfirmationToken() {
        String rawToken;
        Optional<EmailConfirmationToken> optionalToken;

        do {
            rawToken = generateEncodedToken();
            String finalRawToken = rawToken;
            optionalToken = emailConfirmationTokenRepository.findAll().stream()
                    .filter(e -> e.getRawValue() != null &&
                            passwordEncoder.matches(finalRawToken, e.getRawValue()))
                    .findFirst();
        } while (optionalToken.isPresent());

        return rawToken;
    }

    private String generateEncodedToken(){
        return UUID.randomUUID().toString();
    }

    public void deleteAllConfirmationTokensForMember(Member member) {
        emailConfirmationTokenRepository.deleteByMember(member);
    }
}
