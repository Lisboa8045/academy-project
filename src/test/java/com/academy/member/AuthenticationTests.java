package com.academy.member;

import com.academy.config.TestConfig;
import com.academy.config.TestTokenStorage;
import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.exceptions.UnavailableUserException;
import com.academy.models.Role;
import com.academy.models.member.Member;
import com.academy.repositories.RoleRepository;
import com.academy.services.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class AuthenticationTests {

    private final String username = "user123453167891212";
    private final String email = "user123453167891212@gmail.com";

    @Autowired private RoleRepository roleRepository;
    @Autowired MemberService memberService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired TestTokenStorage testTokenStorage;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationFacade authenticationFacade; // ✅ MockBean, not Autowired constructor

    @BeforeEach
    public void setup() throws BadRequestException {
        Role role = new Role();
        role.setName("ADMIN");
        roleRepository.save(role);
    }

    private Long register(){


        RegisterRequestDto registerRequest = new RegisterRequestDto(
                username,
                "Password123@",
                email,
                1L,
                null,
                null,
                null
        );

        return memberService.register(registerRequest);
    }
    private void confirmEmail(){
        String rawConfirmationToken = testTokenStorage.getLastToken();
        memberService.confirmEmail(rawConfirmationToken);
    }

   @Test
    public void registerAndConfirmEmail(){
        Long memberId = register();
        Member member = memberService.getMemberEntityById(memberId);
        assertFalse(member.isEnabled());
        String rawConfirmationToken = testTokenStorage.getLastToken();
        memberService.confirmEmail(rawConfirmationToken);
        member = memberService.getMemberEntityById(memberId);
        assertTrue(member.isEnabled());
    }

    @Test
    public void testLoginWithoutConfirmingEmail() {
        register();
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        assertThrows(UnavailableUserException.class, () -> memberService.login(new LoginRequestDto(
                username,
                "Password123@"
        ), response));
    }

    @Test
    public void testLogin() {
        register();
        confirmEmail();
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        memberService.login(new LoginRequestDto(
                username,
                "Password123@"
        ), response);
    }

    @Test
    public void testLoginAndRecreateConfirmationToken(){
        register();
        memberService.recreateConfirmationToken(username);
        confirmEmail();
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        memberService.login(new LoginRequestDto(
                username,
                "Password123@"
        ), response);
    }

    @Test
    public void testResetPassword(){
        long memberId = register();
        confirmEmail();
        memberService.createPasswordResetToken(email);

        Member member = memberService.getMemberEntityById(memberId);
        assertTrue(passwordEncoder.matches("Password123@", member.getPassword()));

        String rawPasswordResetToken = testTokenStorage.getLastToken();
        memberService.verifyPasswordResetToken(rawPasswordResetToken);
        memberService.resetPassword(rawPasswordResetToken, "newPassword123@");

        Member updatedMember = memberService.getMemberEntityById(memberId);

        assertTrue(passwordEncoder.matches("newPassword123@", updatedMember.getPassword()));
    }

    @Test
    public void testUsingInvalidResetPasswordToken(){
        register();
        confirmEmail();
        memberService.createPasswordResetToken(email);

        assertThatThrownBy(() -> memberService.verifyPasswordResetToken("adbadgbdfadgfafdagrw4"))
                .isInstanceOf(com.academy.exceptions.BadRequestException.class);
    }


}

