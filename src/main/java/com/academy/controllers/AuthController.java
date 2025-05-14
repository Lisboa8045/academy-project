package com.academy.controllers;

import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.LoginResponseDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.register.RegisterResponseDto;
import com.academy.models.Member;
import com.academy.services.JwtUtil;
import com.academy.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private MemberService memberService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(MemberService memberService, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto request){
        long memberId = memberService.register(request);
        return ResponseEntity.ok(new RegisterResponseDto("Member registered successfully!", memberId));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request){
        String token = memberService.login(request);
        return ResponseEntity.ok(new LoginResponseDto("Login successful!", token));
    }

}
