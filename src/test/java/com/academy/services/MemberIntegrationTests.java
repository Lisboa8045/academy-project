package com.academy.services;

import com.academy.dtos.member.MemberRequestDTO;
import com.academy.models.Role;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class MemberIntegrationTests {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public MemberIntegrationTests(MemberService memberService, MemberRepository memberRepository, RoleRepository roleRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("ADMIN");
        role = roleRepository.save(role);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        roleRepository.deleteAll();
    }

    private MemberRequestDTO createMemberRequestDTO(String email, String address, String postalCode, String phoneNumber, String password, Long roleId) {
        return new MemberRequestDTO(email, address, postalCode, phoneNumber, password, roleId);
    }

    @Test
    void createMember() {
    }
}
