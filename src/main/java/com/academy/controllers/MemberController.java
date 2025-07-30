package com.academy.controllers;

import com.academy.config.authentication.JwtCookieUtil;
import com.academy.config.authentication.JwtUtil;
import com.academy.dtos.appointment.AppointmentReviewResponseDTO;
import com.academy.dtos.member.MemberRequestDTO;
import com.academy.dtos.member.MemberResponseDTO;
import com.academy.dtos.register.MemberMapper;
import com.academy.services.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("members")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtCookieUtil jwtCookieUtil;
    private final MemberMapper memberMapper;

    public MemberController(MemberService memberService, JwtUtil jwtUtil, UserDetailsService userDetailsService, JwtCookieUtil jwtCookieUtil, MemberMapper memberMapper) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtCookieUtil = jwtCookieUtil;
        this.memberMapper = memberMapper;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> editMember(@PathVariable long id, @RequestBody MemberRequestDTO memberRequestDTO, HttpServletResponse response){
        MemberResponseDTO memberResponseDTO = memberService.editMember(id, memberRequestDTO);
        if (memberRequestDTO.username() != null) {
            UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(memberRequestDTO.username());
            String newToken = jwtUtil.generateToken(updatedUserDetails);
            jwtCookieUtil.addJwtCookie(response, newToken);
        }
        return ResponseEntity.ok(memberResponseDTO);
    }

    @GetMapping
    public List<MemberResponseDTO> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/byUsername/{username}")
    public ResponseEntity<MemberResponseDTO> getMemberByUserName(@PathVariable String username) {
        return ResponseEntity.ok(memberService.getMemberDTOByUsername(username));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberResponseDTO>> searchMembersByContainsUsernameAndRoleName(@RequestParam String username,
                                                                                              @RequestParam String roleName) {
        return ResponseEntity.ok(memberService.searchByUsernameAndRole(username, roleName).stream().map(memberMapper::toResponseDTO).toList());
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<AppointmentReviewResponseDTO>> getReviewsByMemberId(@PathVariable Long id) {
        List<AppointmentReviewResponseDTO> reviews = memberService.getReviewsByMemberId(id);
        return ResponseEntity.ok(reviews);
    }
}
