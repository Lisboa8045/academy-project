package com.academy.security;

import com.academy.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberSecurity {

    private final MemberService memberService;

    @Autowired
    public MemberSecurity(MemberService memberService) {
        this.memberService = memberService;
    }

    public boolean isSelf(Long memberId, String username) {
        return memberService.isMemberSelf(memberId, username);
    }
}
