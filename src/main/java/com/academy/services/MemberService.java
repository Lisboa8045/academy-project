package com.academy.services;

import com.academy.dtos.register.MemberMapper;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.exceptions.EntityAlreadyExists;
import com.academy.exceptions.NotFoundException;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class MemberService {
    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;;
    private RoleRepository roleRepository;
    private MemberMapper memberMapper;
    private JwtUtil jwtUtil;
    private MessageSource messageSource;

    @Autowired
    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         RoleRepository roleRepository,
                MemberMapper memberMapper,
                         JwtUtil jwtUtil,
                         MessageSource messageSource) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.memberMapper = memberMapper;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
    }

    public long register(RegisterRequestDto request) {
        if (memberRepository.findByUsername(request.username()).isPresent()
                || memberRepository.findByEmail(request.email()).isPresent())
            throw new EntityAlreadyExists(messageSource.getMessage("user.exists", null, LocaleContextHolder.getLocale()));
        if (!isValidPassword(request.password()))
            throw new InvalidArgumentException(messageSource.getMessage("register.invalidpassword", null, LocaleContextHolder.getLocale()));
        Optional<Role> optionalRole = roleRepository.findById(request.roleId());
        if(optionalRole.isEmpty())
            throw new NotFoundException(messageSource.getMessage("role.notfound", null, LocaleContextHolder.getLocale()));
        Member member = memberMapper.toMember(request);
        member.setPassword(passwordEncoder.encode(request.password()));
        member.setRole(optionalRole.get());
        memberRepository.save(member);
        return member.getId();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

    }

    public String login(LoginRequestDto request) {
        Optional<Member> member = memberRepository.findByUsername(request.username());
        if(member.isPresent() && passwordEncoder.matches(request.password(), member.get().getPassword())) {
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    member.get().getUsername(), member.get().getPassword(), new ArrayList<>()
            );
            String token = jwtUtil.generateToken(userDetails);
            return token;
        }
        throw new AuthenticationException(messageSource.getMessage("auth.invalid", null, LocaleContextHolder.getLocale()));

    }
}
