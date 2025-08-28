package com.academy.controllers;

import com.academy.dtos.member.AutoLoginResponseDTO;
import com.academy.dtos.register.ConfirmEmailResponseDto;
import com.academy.dtos.register.CreatePasswordResetTokenRequestDto;
import com.academy.dtos.register.CreatePasswordResetTokenResponseDto;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.LoginResponseDto;
import com.academy.dtos.register.PasswordResetRequestDto;
import com.academy.dtos.register.PasswordResetResponseDto;
import com.academy.dtos.register.RecreateAccountDeletionTokenRequestDto;
import com.academy.dtos.register.RecreateAccountDeletionTokenResponseDto;
import com.academy.dtos.register.RecreateConfirmationTokenRequestDto;
import com.academy.dtos.register.RecreateConfirmationTokenResponseDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.register.RegisterResponseDto;
import com.academy.exceptions.MaxTokensException;
import com.academy.services.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final MessageSource messageSource;

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
    public ResponseEntity<AutoLoginResponseDTO> autoLogin() {
        AutoLoginResponseDTO response = memberService.attemptAutoLogin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recreate-confirmation-token")
    public ResponseEntity<RecreateConfirmationTokenResponseDto> recreateConfirmationToken(
            @RequestBody RecreateConfirmationTokenRequestDto request) {
        memberService.recreateConfirmationToken(request.login());
        return ResponseEntity.ok(new RecreateConfirmationTokenResponseDto("Confirmation token recreated"));
    }

    @PostMapping("/recreate-delete-account-token")
    public ResponseEntity<RecreateAccountDeletionTokenResponseDto> recreateAccountDeletionToken(
            @RequestBody RecreateAccountDeletionTokenRequestDto request) {
        memberService.recreateDeletionToken(request.login());
        return ResponseEntity.ok(new RecreateAccountDeletionTokenResponseDto("Account deletion token recreated"));
    }

    @PostMapping("/password-reset-token")
    public ResponseEntity<CreatePasswordResetTokenResponseDto> createPasswordResetToken(
            @RequestBody CreatePasswordResetTokenRequestDto request) {
        try {
            memberService.createPasswordResetToken(request.email());
        } catch (MaxTokensException ignored) {
            // Always return success response to avoid leaking info
        }
        return ResponseEntity.ok(new CreatePasswordResetTokenResponseDto("Password reset token created"));
    }
}
