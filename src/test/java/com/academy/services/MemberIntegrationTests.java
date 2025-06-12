package com.academy.services;

import com.academy.dtos.member.MemberRequestDTO;
import com.academy.dtos.member.MemberResponseDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.models.Member;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;


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

    private Member member = new Member();

    @BeforeEach
    void setUp() {

        Role role1 = new Role();
        role1.setName("ADMIN");
        role1 = roleRepository.save(role1);

        Role role2 = new Role();
        role2.setName("CLIENT");
        role2 = roleRepository.save(role2);

        member.setUsername("Mestre Splinter");
        member.setEmail("mestre.splinter@gmail.com");
        member.setRole(role1);
        member.setPassword("MestreSplinter123!");
        memberRepository.save(member);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        roleRepository.deleteAll();
    }

    private MemberRequestDTO createMemberRequestDTO(String username, String email, String address, String postalCode, String phoneNumber, String password, Long roleId) {
        return new MemberRequestDTO(username, email, address, postalCode, phoneNumber, password, roleId);
    }
@Test
    void deleteMember_memberNotFound_throwsException(){
        assertThatThrownBy(() -> memberService.deleteMember(999))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteExisingMember_shouldDeleteSuccessfully(){
        memberService.deleteMember(member.getId());
        assertFalse(memberRepository.existsById(member.getId()));
    }

    @Test
    void editMember_memberNotFound_throwsException(){
        MemberRequestDTO memberRequestDTO = createMemberRequestDTO(member.getUsername(), member.getEmail(), member.getAddress(), member.getPostalCode(), member.getPhoneNumber(), member.getPassword(), member.getId());
        assertThatThrownBy(() -> memberService.editMember(999, memberRequestDTO))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void editMember_shouldEditSuccessfully(){
        MemberRequestDTO memberRequestDTO = createMemberRequestDTO("Donatello", "donatello@example.com",
                "Esgoto", "0000-100",
                "987654321", "pizza4EverEnjoyer!", 2L);
        MemberResponseDTO memberResponseDTO = memberService.editMember(member.getId(), memberRequestDTO);

        assertThat(memberResponseDTO.address()).isEqualTo("Esgoto");
        assertThat(memberResponseDTO.postalCode()).isEqualTo("0000-100");
        assertThat(memberResponseDTO.phoneNumber()).isEqualTo("987654321");
        assertThat(memberResponseDTO.email()).isNotEqualTo("donatello@example.com");
        assertThat(memberResponseDTO.role()).isEqualTo("CLIENT");
    }

    @Test
    void editMember_roleNotFound_throwsException(){
        MemberRequestDTO memberRequestDTO = createMemberRequestDTO(
                "Donatello",
                "donatello@example.com",
                "Esgoto", "0000-100",
                "987654321",
                "pizza4EverEnjoyer!",
                99L);
        assertThatThrownBy(() -> memberService.editMember(member.getId(), memberRequestDTO))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void editMember_withSameData_shouldUpdateSuccessfully(){
        MemberRequestDTO request = createMemberRequestDTO(
                member.getUsername(),
                member.getEmail(),
                member.getAddress(),
                member.getPostalCode(),
                member.getPhoneNumber(),
                member.getPassword(),
                member.getRole().getId()
        );
        MemberResponseDTO memberResponseDTO = memberService.editMember(member.getId(), request);

        assertThat(memberResponseDTO.username()).isEqualTo(member.getUsername());
        assertThat(memberResponseDTO.address()).isEqualTo(member.getAddress());
        assertThat(memberResponseDTO.email()).isEqualTo(member.getEmail());
        assertThat(memberResponseDTO.postalCode()).isEqualTo(member.getPostalCode());
        assertThat(memberResponseDTO.phoneNumber()).isEqualTo(member.getPhoneNumber());
        assertThat(memberResponseDTO.role()).isEqualTo(member.getRole().getName());
    }

    @Test
    void editMember_withNullFields_shouldWork(){
        MemberRequestDTO memberRequestDTO = createMemberRequestDTO(null,null,
                null, null,
                "987654321", "pizza4EverEnjoyer!", 1L);

        MemberResponseDTO response = memberService.editMember(member.getId(), memberRequestDTO);
        assertThat(response.email()).isEqualTo(member.getEmail());
        assertThat(response.address()).isEqualTo(member.getAddress());
        assertThat(response.postalCode()).isEqualTo(member.getPostalCode());
        assertThat(response.phoneNumber()).isEqualTo("987654321");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void editMember_toAnotherRole_shouldWork(){
        MemberRequestDTO memberRequestDTO = createMemberRequestDTO(null,null,
                null, null,
                null, null, 2L);
        MemberResponseDTO response = memberService.editMember(member.getId(), memberRequestDTO);
        assertThat(response.role()).isEqualTo("CLIENT");
    }

    @Test
    void getMemberById_shouldReturnCorrectMember() {
        MemberResponseDTO response = memberService.getMemberById(member.getId());

        assertThat(response.email()).isEqualTo(member.getEmail());
        assertThat(response.address()).isEqualTo(member.getAddress());
    }

    @Test
    void getMember_invalidId_throwsException() {
        assertThatThrownBy(() -> memberService.getMemberById(999))
                .isInstanceOf(EntityNotFoundException.class);
    }


}
