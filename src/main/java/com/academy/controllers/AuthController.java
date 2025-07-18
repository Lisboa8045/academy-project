package com.academy.controllers;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.register.*;
import com.academy.models.member.Member;
import com.academy.services.EmailService;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.LoginResponseDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.register.RegisterResponseDto;
import com.academy.services.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final MessageSource messageSource;
    private final EmailService emailService;
    private final AuthenticationFacade authenticationFacade;

    @Autowired
    public AuthController(MemberService memberService, MessageSource messageSource, EmailService emailService, AuthenticationFacade authenticationFacade) {
        this.memberService = memberService;
        this.messageSource = messageSource;
        this.emailService = emailService;
        this.authenticationFacade = authenticationFacade;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request){
        long memberId = memberService.register(request);
        return ResponseEntity.ok(
                new RegisterResponseDto(
                        messageSource.getMessage("user.registered", null, LocaleContextHolder.getLocale()),
                        memberId
                )
        );
    }

    @GetMapping("/confirm-email/{token}")
    public ResponseEntity<ConfirmEmailResponseDto> confirmEmail(@PathVariable String token) {
       memberService.confirmEmail(token);
       return ResponseEntity.ok(new ConfirmEmailResponseDto("Email confirmed successfully"));
    }

    @GetMapping("/password-reset/{token}")
    public ResponseEntity<PasswordResetResponseDto> verifyPasswordResetToken(@PathVariable String token) {
        memberService.verifyPasswordResetToken(token);
        return ResponseEntity.ok(new PasswordResetResponseDto("Password can be reset"));
    }

    @PatchMapping("/password-reset/{token}")
    public ResponseEntity<PasswordResetResponseDto> resetPassword(@PathVariable String token, @RequestBody PasswordResetRequestDto requestDto) {
        memberService.resetPassword(token, requestDto.newPassword());
        return ResponseEntity.ok(new PasswordResetResponseDto("Password reset successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse httpResponse){
        LoginResponseDto response = memberService.login(request, httpResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        memberService.logout(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = authenticationFacade.getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

        if ("anonymousUser".equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Member member = memberService.getMemberByUsername(username);
        Long id = member.getId();
        String profilePicture = member.getProfilePicture();
        String role =  member.getRole().getName();

        return ResponseEntity.ok(Map.of("username", username, "id", id, "profilePicture", profilePicture != null ? profilePicture : "", "role", role));
    }

    @PostMapping("/recreate-confirmation-token")
    public ResponseEntity<RecreateConfirmationTokenResponseDto> recreateConfirmationToken(
            @RequestBody RecreateConfirmationTokenRequestDto request) {
        memberService.recreateConfirmationToken(request.login());
        return ResponseEntity.ok(new RecreateConfirmationTokenResponseDto("Confirmation token recreated"));
    }

    @PostMapping("/password-reset-token")
    public ResponseEntity<CreatePasswordResetTokenResponseDto> createPasswordResetToken(
            @RequestBody CreatePasswordResetTokenRequestDto request) {
        memberService.createPasswordResetToken(request.email());
        return ResponseEntity.ok(new CreatePasswordResetTokenResponseDto("Password reset token created"));
    }
}
