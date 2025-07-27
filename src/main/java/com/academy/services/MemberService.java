package com.academy.services;

import com.academy.config.AppProperties;
import com.academy.config.TestTokenStorage;
import com.academy.config.authentication.JwtCookieUtil;
import com.academy.config.authentication.JwtUtil;
import com.academy.dtos.member.MemberRequestDTO;
import com.academy.dtos.member.MemberResponseDTO;
import com.academy.dtos.register.LoginRequestDto;
import com.academy.dtos.register.LoginResponseDto;
import com.academy.dtos.register.MemberMapper;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.BadRequestException;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.exceptions.MemberNotFoundByEmailException;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.exceptions.NotFoundException;
import com.academy.exceptions.RegistrationConflictException;
import com.academy.exceptions.TokenExpiredException;
import com.academy.exceptions.UnavailableUserException;
import com.academy.models.Role;
import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;
    private final EmailService emailService;
    private final GlobalConfigurationService globalConfigurationService;
    private final AppProperties appProperties;
    @Autowired(required = false)
    private TestTokenStorage testTokenStorage;

    private final JwtCookieUtil jwtCookieUtil;
    private final ServiceProviderService serviceProviderService;

    @Autowired
    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         RoleRepository roleRepository,
                         MemberMapper memberMapper,
                         JwtUtil jwtUtil,
                         MessageSource messageSource,
                         JwtCookieUtil jwtCookieUtil,
                         EmailService emailService,
                         GlobalConfigurationService globalConfigurationService,
                         AppProperties appProperties,
                         @Lazy ServiceProviderService serviceProviderService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.memberMapper = memberMapper;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
        this.jwtCookieUtil = jwtCookieUtil;
        this.emailService = emailService;
        this.globalConfigurationService = globalConfigurationService;
        this.appProperties = appProperties;
        this.serviceProviderService = serviceProviderService;
    }


    public void logout(HttpServletResponse response){
        jwtCookieUtil.clearJwtCookie(response);
    }

    @Transactional
    public long register(RegisterRequestDto request) {
        Map<String, String> errors = new HashMap<>();
        if (memberRepository.findByUsername(request.username()).isPresent()) {
            String message = messageSource.getMessage("username.exists", null, LocaleContextHolder.getLocale());
            errors.put("username", message);
        }
        if (memberRepository.findByEmail(request.email()).isPresent()) {
            String message = messageSource.getMessage("email.exists", null, LocaleContextHolder.getLocale());
            errors.put("email", message);
        }
        if (!errors.isEmpty()) {
            throw new RegistrationConflictException(errors);
        }

        if (!isValidPassword(request.password()))
            throw new InvalidArgumentException(messageSource.getMessage("register.invalidpassword", null, LocaleContextHolder.getLocale()));
        Optional<Role> optionalRole = roleRepository.findById(request.roleId());

        if(optionalRole.isEmpty())
            throw new NotFoundException(messageSource.getMessage("role.notfound", null, LocaleContextHolder.getLocale()));

        return createMember(request, optionalRole.get()).getId();

    }
    private Member createMember(RegisterRequestDto request, Role role){
        Member member = memberMapper.toMember(request);
        member.setPassword(passwordEncoder.encode(request.password()));
        member.setRole(role);
        member.setEnabled(false);
        member.setStatus(MemberStatusEnum.WAITING_FOR_EMAIL_APPROVAL);
        String rawConfirmationToken = generateUniqueConfirmationToken();
        if (testTokenStorage != null) {
            testTokenStorage.storeToken(rawConfirmationToken);
        }
        member.setConfirmationToken(passwordEncoder.encode(rawConfirmationToken));
        member.setTokenExpiry(LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("confirmation_token_expiry_minutes"))));
        memberRepository.save(member);

        emailService.sendConfirmationEmail(member, rawConfirmationToken);
        return member;
    }

    private String generateUniqueConfirmationToken() {
        String rawToken;
        Optional<Member> optionalMember;

        do {
            rawToken = generateEncodedToken();
            String finalRawToken = rawToken;
            optionalMember = memberRepository.findAll().stream()
                    .filter(m -> m.getConfirmationToken() != null &&
                            passwordEncoder.matches(finalRawToken, m.getConfirmationToken()))
                    .findFirst();
        } while (optionalMember.isPresent());

        return rawToken;
    }

    private String generateUniquePasswordResetToken() {
        String rawToken;
        Optional<Member> optionalMember;

        do {
            rawToken = generateEncodedToken();
            String finalRawToken = rawToken;
            optionalMember = memberRepository.findAll().stream()
                    .filter(m -> m.getPasswordResetToken() != null &&
                            passwordEncoder.matches(finalRawToken, m.getPasswordResetToken()))
                    .findFirst();
        } while (optionalMember.isPresent());

        return rawToken;
    }

    private String generateEncodedToken(){
        return UUID.randomUUID().toString();
    }
    @Transactional
    public void confirmEmail(String confirmationToken) {
        Member member = memberRepository.findAll().stream()
                .filter(m -> m.getConfirmationToken() != null &&
                        passwordEncoder.matches(confirmationToken, m.getConfirmationToken()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Confirmation Token is Invalid/Not found"));

        if (member.getTokenExpiry().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Confirmation Token has Expired");

        member.setConfirmationToken(null);
        member.setTokenExpiry(null);
        member.setEnabled(true);
        member.setStatus(MemberStatusEnum.ACTIVE);
        memberRepository.save(member);
    }


    public Member verifyPasswordResetToken(String passwordResetToken) {
        Member member = memberRepository.findAll().stream()
                .filter(m -> m.getPasswordResetToken() != null &&
                        passwordEncoder.matches(passwordResetToken, m.getPasswordResetToken()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Password Reset Token is Invalid/Not found"));

        if (member.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Password Reset Token has expired. Please request a new email.");

        return member;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

    }

    private Optional<Member> getMemberByLogin(String login) {
        return login.contains("@")
                ? memberRepository.findByEmail(login)
                : memberRepository.findByUsername(login);
    }

    private Member tryToAuthenticateMember(String login, String password) {
        Optional<Member> optionalMember = getMemberByLogin(login);

        if(optionalMember.isEmpty() ||  !passwordEncoder.matches(password, optionalMember.get().getPassword()))
            throw new AuthenticationException(messageSource.getMessage("auth.invalid", null, LocaleContextHolder.getLocale()));

        return optionalMember.get();
    }
    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {
            Member member = tryToAuthenticateMember(request.login(), request.password());

            if(!member.isEnabled())
                throw new UnavailableUserException(member.getStatus(), member.getEmail());
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    member.getUsername(), member.getPassword(), new ArrayList<>()
            );
            String token = jwtUtil.generateToken(userDetails);
            jwtCookieUtil.addJwtCookie(response, token);
            return new LoginResponseDto(
                    messageSource.getMessage("user.loggedin", null, LocaleContextHolder.getLocale()),
                    token,
                    member.getId(),
                    member.getUsername(),
                    member.getProfilePicture(),
                    member.getRole().getName()
            );
    }

    public Member getMemberByEmail(String email){
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if(optionalMember.isEmpty())
            throw new MemberNotFoundByEmailException(email);
        return optionalMember.get();
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
        System.out.println("Tentando apagar membro com id: " + id);
        Member member = getMemberEntityById(id);
        member.setEnabled(false);
        member.setStatus(MemberStatusEnum.PENDING_DELETION);
        member.setTokenExpiry(LocalDateTime.now().plusDays(
                Integer.parseInt(globalConfigurationService.getConfigValue("account_deletion_expiry_days"))));
        memberRepository.save(member);
        emailService.sendDeleteAccountConfirmationEmail(member, generateRevertToken(member));
    }

    private String generateRevertToken(Member member) {
        String rawToken;
        Optional<Member> optionalMember;

        do {
            rawToken = generateEncodedToken();
            String finalRawToken = rawToken;
            optionalMember = memberRepository.findAll().stream()
                .filter(m -> m.getConfirmationToken() != null && passwordEncoder.matches(finalRawToken, m.getConfirmationToken()))
                .findFirst();
        } while (optionalMember.isPresent());

        member.setConfirmationToken(passwordEncoder.encode(rawToken));
        memberRepository.save(member);
        return rawToken;
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

        if(memberRequestDTO.username() != null){
            member.setUsername(memberRequestDTO.username());
        }

        if(memberRequestDTO.roleId() != null){
            Role newRole = roleRepository.findById(memberRequestDTO.roleId())
                    .orElseThrow(() -> new EntityNotFoundException(Role.class, memberRequestDTO.roleId()));
            member.setRole(newRole);
        }

        if(memberRequestDTO.oldPassword() != null){
            if(!passwordEncoder.matches(memberRequestDTO.oldPassword(), member.getPassword()))
                throw new AuthenticationException("Incorrect password");
            if(!isValidPassword(memberRequestDTO.newPassword()))
                throw new InvalidArgumentException(messageSource.getMessage("register.invalidpassword", null, LocaleContextHolder.getLocale()));

            member.setPassword(passwordEncoder.encode(memberRequestDTO.newPassword()));
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
        return memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Member.class, id));
    }

    public void recreateConfirmationToken(String login) {
        Optional<Member> optionalMember = getMemberByLogin(login);
        if(optionalMember.isEmpty())
            throw new EntityNotFoundException(Member.class, login);
        Member member = optionalMember.get();

        if(member.isEnabled())
            throw new BadRequestException("Member already enabled");
        member.setTokenExpiry(LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("confirmation_token_expiry_minutes"))));
        String rawConfirmationToken = generateUniqueConfirmationToken();
        if (testTokenStorage != null) {
            testTokenStorage.storeToken(rawConfirmationToken);
        }
        member.setConfirmationToken(passwordEncoder.encode(rawConfirmationToken));
        memberRepository.save(member);
        emailService.sendConfirmationEmail(member, rawConfirmationToken);
    }

    public void saveProfilePic(Long id, String filename) {
        memberRepository.findById(id)
                .map(m -> {
                    m.setProfilePicture(filename);
                    memberRepository.save(m);
                    return m;
                })
                .orElseThrow(() -> new EntityNotFoundException(Member.class, id));
    }

    private void unlinkServiceProviders(long id) {
        List<ServiceProvider> serviceProviders = serviceProviderService.getAllByProviderId(id);

        for (ServiceProvider sp : serviceProviders) {
            sp.setProvider(null);
        }
    }

    public void createPasswordResetToken(String email) {
        Member member = getMemberByEmail(email);
        String rawPasswordResetToken = generateUniquePasswordResetToken();
        if (testTokenStorage != null) {
            testTokenStorage.storeToken(rawPasswordResetToken);
        }
        member.setPasswordResetToken(passwordEncoder.encode(rawPasswordResetToken));
        member.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("password_reset_token_expiry_minutes"))));
        memberRepository.save(member);

        emailService.sendPasswordResetEmail(member, rawPasswordResetToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Member member = verifyPasswordResetToken(token);
        if (!isValidPassword(newPassword)) {
            throw new InvalidArgumentException(messageSource.getMessage("register.invalidpassword", null, LocaleContextHolder.getLocale()));
        }
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setPasswordResetToken(null);
        member.setPasswordResetTokenExpiry(null);
        memberRepository.save(member);
    }


    @Transactional
    public void revertDelete(String token) {
        Member member = memberRepository.findAll().stream()
            .filter(m -> m.getConfirmationToken() != null &&
                    passwordEncoder.matches(token, m.getConfirmationToken()) &&
                    m.getStatus() == MemberStatusEnum.PENDING_DELETION)
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Invalid or expired revert token."));

        if (member.getTokenExpiry() == null || member.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Revert token has expired.");
        }

        member.setEnabled(true);
        member.setStatus(MemberStatusEnum.ACTIVE);
        member.setConfirmationToken(null);
        member.setTokenExpiry(null);
        memberRepository.save(member);
    }

    @Scheduled(cron = "0 */2 * * * ?") // Every 2 minutes
    public void permanentlyDeleteExpiredAccounts() {
        List<Member> expiredMembers = memberRepository.findAll().stream()
                .filter(m -> m.getStatus() == MemberStatusEnum.PENDING_DELETION
                        && m.getTokenExpiry() != null
                        && m.getTokenExpiry().isBefore(LocalDateTime.now()))
                .toList();

        memberRepository.deleteAll(expiredMembers);
    }
}
