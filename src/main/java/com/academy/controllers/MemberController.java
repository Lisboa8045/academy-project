package com.academy.controllers;

import com.academy.dtos.member.MemberRequestDTO;
import com.academy.dtos.member.MemberResponseDTO;
import com.academy.services.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> editMember(@PathVariable long id, @RequestBody MemberRequestDTO memberRequestDTO){
        MemberResponseDTO memberResponseDTO = memberService.editMember(id, memberRequestDTO);
        return ResponseEntity.ok(memberResponseDTO);
    }
}
