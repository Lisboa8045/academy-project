package com.academy.services;

import com.academy.config.authentication.JwtUtil;
import com.academy.dtos.member.MemberRequestDTO;
import com.academy.dtos.member.MemberResponseDTO;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.LoginResponseDto;
import com.academy.dtos.register.MemberMapper;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.exceptions.*;
import com.academy.models.Availability;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;

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
    public void logout(HttpServletResponse response){
        System.out.println("Backend 2 logout");
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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

    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {
        Optional<Member> member = request.login().contains("@")
                ? memberRepository.findByEmail(request.login())
                : memberRepository.findByUsername(request.login());

        if(member.isPresent() && passwordEncoder.matches(request.password(), member.get().getPassword())) {
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    member.get().getUsername(), member.get().getPassword(), new ArrayList<>()
            );
            String token = jwtUtil.generateToken(userDetails);
            System.out.println("Token generated:" + token);
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);
            response.addCookie(cookie);
            return new LoginResponseDto(
                    messageSource.getMessage("user.loggedin", null, LocaleContextHolder.getLocale()),
                    token,
                    member.get().getId(),
                    member.get().getUsername()
            );
        }
        throw new AuthenticationException(messageSource.getMessage("auth.invalid", null, LocaleContextHolder.getLocale()));
    }

    public Member getMemberByUsername(String username){
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        if(optionalMember.isEmpty())
            throw new MemberNotFoundException(username);
        return optionalMember.get();
    }

    public boolean existsById(Long memberId) {
        return memberRepository.existsById(memberId);
    }

    public Optional<Member> findbyId(long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException(Member.class, memberId);
        }
        return memberRepository.findById(memberId);
    }

    public void deleteMember(long id) {
        if(!memberRepository.existsById(id)) throw new EntityNotFoundException(Member.class,id);

        memberRepository.deleteById(id);
    }

    public MemberResponseDTO editMember(long id, MemberRequestDTO memberRequestDTO){
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, id));

        if(memberRequestDTO.address() != null){
            member.setAddress(memberRequestDTO.address());
        }

        if(memberRequestDTO.postalCode() != null){
            member.setPostalCode(memberRequestDTO.postalCode());
        }

        if(memberRequestDTO.phoneNumber() != null){
            member.setPhoneNumber(memberRequestDTO.phoneNumber());
        }

        if(memberRequestDTO.email() != null){
            member.setEmail(memberRequestDTO.email());
        }

        if(memberRequestDTO.password() != null){
            member.setPassword(memberRequestDTO.password());
        }

        if(memberRequestDTO.roleId() != null){
            Role newRole = roleRepository.findById(memberRequestDTO.roleId())
                    .orElseThrow(() -> new EntityNotFoundException(Role.class, memberRequestDTO.roleId()));
            member.setRole(newRole);
        }
        return memberMapper.toResponseDTO(memberRepository.save(member));
    }

    public List<MemberResponseDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(memberMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public MemberResponseDTO getMemberById(long id) {
        return memberRepository.findById(id)
                .map(memberMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, id));
    }
    public Member getMemberEntityById(long id){
        return memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(ServiceProvider.class, id));
    }
}
