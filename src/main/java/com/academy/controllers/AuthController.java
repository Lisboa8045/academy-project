package com.academy.controllers;

import com.academy.dtos.register.*;
import com.academy.services.EmailService;
import com.academy.services.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final MessageSource messageSource;
    private final EmailService emailService;

    @Autowired
    public AuthController(MemberService memberService, MessageSource messageSource, EmailService emailService) {
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
    public ResponseEntity<ConfirmEmailResponseDto> confirmEmail(@PathVariable String token) throws Exception {
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
    @PostMapping("/send")
    public String sendEmail(){
       /* String html = loadTemplate()
                .replace("[User Name]", "João")
                .replace("[CONFIRMATION_LINK]", "https://google.com")
                .replace("[App Name]", "Academy Project");

        emailService.send(
                "adriano.l.a.queiroz@gmail.com",
                "Confirme sua conta",
                "Clique no link para confirmar: " + "https://google.com",
                html
        );
        return "Email Sent";

        */
        return "";
    }



    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

        if ("anonymousUser".equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(Map.of("username", username));
    }

}
