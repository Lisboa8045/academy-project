package com.academy.security;

import com.academy.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberSecurity {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberSecurity(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public boolean isSelf(Long memberId, String username) {
        return memberRepository.findById(memberId)
                .map(member -> username.equals(member.getUsername()))
                .orElse(false);
    }
}