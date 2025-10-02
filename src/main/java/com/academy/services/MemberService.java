package com.academy.services;

import com.academy.config.TestTokenStorage;
import com.academy.config.authentication.AuthenticationFacade;
import com.academy.config.authentication.JwtCookieUtil;
import com.academy.config.authentication.JwtUtil;
import com.academy.dtos.appointment.AppointmentMapper;
import com.academy.dtos.appointment.AppointmentReviewResponseDTO;
import com.academy.dtos.member.AutoLoginResponseDTO;
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
import com.academy.exceptions.MaxDailyTokensException;
import com.academy.exceptions.MaxTokensException;
import com.academy.exceptions.MemberNotFoundByEmailException;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.exceptions.NotFoundException;
import com.academy.exceptions.RegistrationConflictException;
import com.academy.exceptions.TokenExpiredException;
import com.academy.exceptions.UnavailableUserException;
import com.academy.models.Role;
import com.academy.models.appointment.Appointment;
import com.academy.models.appointment.AppointmentStatus;
import com.academy.models.member.Member;
import com.academy.models.member.MemberStatusEnum;
import com.academy.models.service.ServiceStatusEnum;
import com.academy.models.token.MemberToken;
import com.academy.models.token.TokenTypeEnum;
import com.academy.repositories.AppointmentRepository;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import com.academy.repositories.ServiceProviderRepository;
import com.academy.repositories.ServiceRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationFacade authenticationFacade;
    private final JwtCookieUtil jwtCookieUtil;
    private final ServiceProviderService serviceProviderService;
    private final MemberTokenService memberTokenService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Autowired(required = false)
    private TestTokenStorage testTokenStorage;

    private final AccountCleanupService accountCleanupService;
    @Autowired
    private ServiceRepository serviceRepository;

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
                         AuthenticationFacade authenticationFacade,
                         AppointmentRepository appointmentRepository,
                         MemberTokenService memberTokenService,
                         @Lazy ServiceProviderService serviceProviderService,
                         ServiceProviderRepository serviceProviderRepository,
                         AppointmentMapper appointmentMapper,
                         AccountCleanupService accountCleanupService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.memberMapper = memberMapper;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
        this.jwtCookieUtil = jwtCookieUtil;
        this.emailService = emailService;
        this.globalConfigurationService = globalConfigurationService;
        this.authenticationFacade = authenticationFacade;
        this.appointmentRepository = appointmentRepository;
        this.serviceProviderService = serviceProviderService;
        this.memberTokenService = memberTokenService;
        this.serviceProviderRepository = serviceProviderRepository;
        this.appointmentMapper = appointmentMapper;
        this.accountCleanupService = accountCleanupService;
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
        member.setDeletionTokensSentToday(0);
        member.setProfilePicture("placeholderavatar.png");
        LocalDateTime expirationDateTime = LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("confirmation_token_expiry_minutes")));

        Member memberSaved = memberRepository.save(member);

        MemberToken emailConfirmationToken = memberTokenService.createToken(memberSaved, expirationDateTime, TokenTypeEnum.EMAIL_CONFIRMATION);
        emailService.sendConfirmationEmail(memberSaved, emailConfirmationToken.getRawValue());
        return member;
    }

    @Transactional
    public void confirmEmail(String confirmationToken) {
        MemberToken token;
        try{
            token = memberTokenService.getTokenByRawValue(confirmationToken, TokenTypeEnum.EMAIL_CONFIRMATION);
        }catch(EntityNotFoundException e){
            throw new BadRequestException("Confirmation Token is Invalid/Not found");
        }

        if (token.getExpirationDate().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Confirmation Token has Expired");
        Member member = token.getMember();

        member.setEnabled(true);
        member.setStatus(MemberStatusEnum.ACTIVE);
        memberRepository.save(member);

        memberTokenService.deleteAllByTokenTypeForMember(member, TokenTypeEnum.EMAIL_CONFIRMATION);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Member member = verifyPasswordResetToken(token);
        if (!isValidPassword(newPassword)) {
            throw new InvalidArgumentException(messageSource.getMessage("register.invalidpassword", null, LocaleContextHolder.getLocale()));
        }
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        memberTokenService.deleteAllByTokenTypeForMember(member, TokenTypeEnum.PASSWORD_RESET);
    }

    public Member verifyPasswordResetToken(String passwordResetToken) {
        MemberToken token;
        try{
            token = memberTokenService.getTokenByRawValue(passwordResetToken, TokenTypeEnum.PASSWORD_RESET);
        }catch(EntityNotFoundException e){
            throw new BadRequestException("Password Reset Token is Invalid/Not found");
        }

        if (token.getExpirationDate().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Password Reset Token has Expired");

        return token.getMember();
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

    public MemberResponseDTO getMemberDTOByUsername(String username) {
        return memberMapper.toResponseDTO(getMemberByUsername(username));
    }

    public Member getMemberByUsername(String username){
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        if(optionalMember.isEmpty())
            throw new MemberNotFoundException(username);
        return optionalMember.get();
    }

    public List<MemberResponseDTO> getMemberDTOByUsernameAndRole(String username, String roleName) {
        return searchByUsernameAndRole(username, roleName).stream().map(memberMapper::toResponseDTO).toList();
    }

    public List<Member> searchByUsernameAndRole(String username, String roleName) {
        return memberRepository.searchMemberByUsernameContainsIgnoreCaseAndRoleNameAndEnabled(username, roleName, true);
    }

    public Optional<Member> findbyId(long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException(Member.class, memberId);
        }
        return memberRepository.findById(memberId);
    }

    @Transactional
    public void deleteMember(long id) {
        Member member = getMemberEntityById(id);
        validateIfReachedMaxTokens(member, TokenTypeEnum.ACCOUNT_DELETION);

        member.setDeletionTokensSentToday(member.getDeletionTokensSentToday() + 1);
        member.setEnabled(false);
        member.setStatus(MemberStatusEnum.PENDING_DELETION);
        serviceRepository.findOwnedAndProvidedByMember(id).forEach(service -> {
            appointmentRepository.cancelAppointmentsByServiceId(service.getId(), AppointmentStatus.CANCELLED);
            service.setEnabled(false);
            service.setStatus(ServiceStatusEnum.DISABLED_OWNER_DELETED);
            serviceRepository.save(service);
        });
        memberRepository.save(member);

        LocalDateTime expirationDateTime = LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("account_deletion_expiry_days")));
        MemberToken accountDeletionToken = memberTokenService.createToken(member, expirationDateTime, TokenTypeEnum.ACCOUNT_DELETION);

        emailService.sendDeleteAccountConfirmationEmail(member, accountDeletionToken.getRawValue());
    }

    @Transactional
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

    @Transactional
    public void recreateConfirmationToken(String login) {
        Optional<Member> optionalMember = getMemberByLogin(login);
        if(optionalMember.isEmpty())
            throw new EntityNotFoundException(Member.class, login);
        Member member = optionalMember.get();

        if(member.isEnabled())
            throw new BadRequestException("Member already enabled");

        validateIfReachedMaxTokens(member, TokenTypeEnum.EMAIL_CONFIRMATION);

        LocalDateTime expirationDateTime = LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("confirmation_token_expiry_minutes")));
        Member memberSaved = memberRepository.save(member);
        MemberToken emailConfirmationToken = memberTokenService.createToken(member, expirationDateTime, TokenTypeEnum.EMAIL_CONFIRMATION);

        emailService.sendConfirmationEmail(memberSaved, emailConfirmationToken.getRawValue());
    }

    @Transactional
    public void saveProfilePic(Long id, String filename) {
        memberRepository.findById(id)
                .map(m -> {
                    m.setProfilePicture(filename);
                    memberRepository.save(m);
                    return m;
                })
                .orElseThrow(() -> new EntityNotFoundException(Member.class, id));
    }

    @Transactional
    public void createPasswordResetToken(String email) {

        Member member = getMemberByEmail(email);
        validateIfReachedMaxTokens(member, TokenTypeEnum.PASSWORD_RESET);

        LocalDateTime expirationDateTime = LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("password_reset_token_expiry_minutes")));

        MemberToken passwordResetToken = memberTokenService.createToken(member, expirationDateTime, TokenTypeEnum.PASSWORD_RESET);
        memberRepository.save(member);

        emailService.sendPasswordResetEmail(member, passwordResetToken.getRawValue());
    }

    public AutoLoginResponseDTO attemptAutoLogin() {
        Authentication auth = authenticationFacade.getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AuthenticationException("User not authenticated");
        }

        String username = auth.getName();
        Member member = getMemberByUsername(username);
        return new AutoLoginResponseDTO(
                member.getId(),
                username,
                member.getProfilePicture() != null ? member.getProfilePicture() : "",
                member.getRole().getName()
        );
    }

    @Transactional
    public void revertAccountDelete(String accountDeletionToken) {
        MemberToken token;
        try {
            token = memberTokenService.getTokenByRawValue(accountDeletionToken, TokenTypeEnum.ACCOUNT_DELETION);
        } catch(EntityNotFoundException e){
            throw new BadRequestException("Account Deletion Token is Invalid/Not found");
        }

        if (token.getExpirationDate().isBefore(LocalDateTime.now()))
            throw new TokenExpiredException("Account Deletion Token has Expired");

        Member member = token.getMember();

        member.setEnabled(true);
        member.setStatus(MemberStatusEnum.ACTIVE);
        memberRepository.save(member);

        serviceRepository.findOwnedAndProvidedByMember(member.getId()).forEach(service -> {
            service.setEnabled(true);
            service.setStatus(ServiceStatusEnum.PENDING_APPROVAL);
        });
        memberTokenService.deleteAllByTokenTypeForMember(member, TokenTypeEnum.ACCOUNT_DELETION);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void permanentlyDeleteExpiredAccounts() {
        accountCleanupService.cleanupExpiredAccounts();
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void resetDailyCounters() {
        memberRepository.resetDeletionTokensSentToday();
    }

    private void validateIfReachedMaxTokens(Member member, TokenTypeEnum tokenType) {
        List<MemberToken> validTokens = memberTokenService.getValidTokensByMemberAndType(member, tokenType);
        int maxValidTokens;
        if (tokenType == TokenTypeEnum.ACCOUNT_DELETION) {
            int dailyTokens = Integer.parseInt(globalConfigurationService.getConfigValue("account_deletion_daily_tokens"));
            int sentTokens = member.getDeletionTokensSentToday();
            if (sentTokens >= dailyTokens) {
                throw new MaxDailyTokensException("Maximum number of daily " + tokenType.name().toLowerCase().replace("_", " ") + " requests reached. Please try again tomorrow.");
            }
            else
                return;
        }
        else
            maxValidTokens = Integer.parseInt(globalConfigurationService.getConfigValue("maximum_valid_tokens"));

        if(validTokens.size() >= maxValidTokens)
            throw new MaxTokensException("Maximum number of " + tokenType.name().toLowerCase().replace("_", " ") + " requests reached.");
    }

    @Transactional
    public void updateMemberRating(Long memberId) {
        Double rating = serviceProviderRepository.findAverageRatingByMemberId(memberId);
        if (rating != null) {
            int roundedRating = Math.toIntExact(Math.round(rating));
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException(Member.class, memberId));
            member.setRating(roundedRating);
            memberRepository.save(member);
        }
    }

    public Page<AppointmentReviewResponseDTO> getReviewsByMemberId(Long id, Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findAllReviewsByMemberId(id, pageable);
        Page<AppointmentReviewResponseDTO> dtoPage = appointments.map(appointmentMapper::toReviewResponseDTO);
        return dtoPage;
    }

    public List<AppointmentReviewResponseDTO> getAllReviewsByMemberId(Long id) {
        List<Appointment> appointments = appointmentRepository.findAllReviewsByMemberId(id);
        return appointments.stream()
                .map(appointmentMapper::toReviewResponseDTO)
                .toList();
    }

    @Transactional
    public void recreateDeletionToken(String login) {
        Optional<Member> optionalMember = getMemberByLogin(login);
        if(optionalMember.isEmpty())
            throw new EntityNotFoundException(Member.class, login);
        Member member = optionalMember.get();

        validateIfReachedMaxTokens(member, TokenTypeEnum.ACCOUNT_DELETION);

        member.setDeletionTokensSentToday(member.getDeletionTokensSentToday() + 1);
        memberRepository.save(member);

        LocalDateTime expirationDateTime = LocalDateTime.now().plusMinutes(
                Integer.parseInt(globalConfigurationService.getConfigValue("account_deletion_expiry_days")));
        MemberToken accountDeletionToken = memberTokenService.createToken(member, expirationDateTime, TokenTypeEnum.ACCOUNT_DELETION);

        emailService.sendConfirmationEmail(member, accountDeletionToken.getRawValue());
    }
}
