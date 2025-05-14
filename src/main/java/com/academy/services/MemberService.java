package com.academy.services;

import com.academy.dtos.register.RegisterRequestDto;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.exceptions.EntityAlreadyExists;
import com.academy.exceptions.NotFoundException;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {
    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;;
    private RoleRepository roleRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public long register(RegisterRequestDto request) {
        if (memberRepository.findByUsername(request.username()).isPresent()
                || memberRepository.findByEmail(request.email()).isPresent())
            throw new EntityAlreadyExists("Username or Email already exists");
        if (!isValidPassword(request.password()))
            throw new InvalidArgumentException("Invalid Password");
        Optional<Role> optionalRole = roleRepository.findById(request.roleId());
        if(optionalRole.isEmpty())
            throw new NotFoundException("Role not found");
        Member member = new Member();
        member.setUsername(request.username());
        member.setPassword(passwordEncoder.encode(request.password()));
        member.setEmail(request.email());
        member.setAddress(request.address());
        member.setPhoneNumber(request.phoneNumber());
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
}
