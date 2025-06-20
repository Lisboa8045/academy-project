package com.academy.member;

import com.academy.config.AppProperties;
import com.academy.config.TestConfig;
import com.academy.config.TestTokenStorage;
import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.RegisterRequestDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.UnavailableUserException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.member.Member;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import com.academy.services.MemberService;
import com.academy.services.ServiceProviderService;
import com.academy.services.ServiceService;
import com.academy.services.ServiceTypeService;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import java.net.URI;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class AuthenticationTests {

    private final String username = "user123453167891212";

    @Autowired private RoleRepository roleRepository;
    @Autowired MemberService memberService;
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
                username + "@gmail.com",
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
        memberService.recreateConfirmationToken(username, "Password123@");
        confirmEmail();
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        memberService.login(new LoginRequestDto(
                username,
                "Password123@"
        ), response);
    }

}

