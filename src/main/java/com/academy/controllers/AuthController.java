package com.academy.controllers;

import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.LoginResponseDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.register.RegisterResponseDto;
import com.academy.services.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private MemberService memberService;
    private MessageSource messageSource;
    @Autowired
    public AuthController(MemberService memberService, MessageSource messageSource) {
        this.memberService = memberService;
        this.messageSource = messageSource;
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
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request){
        String token = memberService.login(request);
        return ResponseEntity.ok(new LoginResponseDto(
                messageSource.getMessage("user.loggedin", null, LocaleContextHolder.getLocale()),
                token));
    }

}
