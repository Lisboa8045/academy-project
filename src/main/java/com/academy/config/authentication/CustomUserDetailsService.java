package com.academy.config.authentication;

import com.academy.models.member.Member;
import com.academy.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Autowired
    public CustomUserDetailsService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + member.getRole().getName());
        List<GrantedAuthority> authorities = Collections.singletonList(authority);


        return new org.springframework.security.core.userdetails.User(
                member.getUsername(),
                member.getPassword(),
                member.isEnabled(),
                true, true, true,
                authorities
        );
    }

}
