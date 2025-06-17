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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final MessageSource messageSource;
    private final EmailService emailService;

    @Autowired
    public AuthController(MemberService memberService, MessageSource messageSource, EmailService emailService, AuthenticationFacade authenticationFacade) {
        this.memberService = memberService;
        this.messageSource = messageSource;
        this.emailService = emailService;
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
       return  ResponseEntity.ok(new ConfirmEmailResponseDto("Email confirmed successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse httpResponse){
        LoginResponseDto response = memberService.login(request, httpResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        System.out.println("Backend Logout");
        memberService.logout(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

        return ResponseEntity.ok(Map.of("username", username, "id", id, "profilePicture", profilePicture != null ? profilePicture : ""));
    }

    @PostMapping("/recreate-confirmation-token")
    public ResponseEntity<RecreateConfirmationTokenResponseDto> recreateConfirmationToken(
            @RequestBody RecreateConfirmationTokenRequestDto request) {
        memberService.recreateConfirmationToken(request.login(), request.password());
        return ResponseEntity.ok(new RecreateConfirmationTokenResponseDto("Confirmation token recreated"));
    }
}
