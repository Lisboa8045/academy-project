package com.academy.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.availability.*;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.availability.Availability;
import com.academy.models.availability.MemberAvailability;
import com.academy.models.member.Member;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.AvailabilityRepository;
import com.academy.repositories.MemberAvailabilityRepository;
import com.academy.repositories.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final MemberService memberService;
    private final AvailabilityMapper availabilityMapper;
    private final ServiceProviderService serviceProviderService;
    private final AuthenticationFacade authenticationFacade;
    private final MemberAvailabilityRepository memberAvailabilityRepository;

    @Autowired
    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            MemberService memberService,
            AvailabilityMapper availabilityMapper, ServiceProviderService serviceProviderService,
            AuthenticationFacade authenticationFacade,
            MemberAvailabilityRepository memberAvailabilityRepository) {
        this.availabilityRepository = availabilityRepository;
        this.memberService = memberService;
        this.availabilityMapper = availabilityMapper;
        this.serviceProviderService = serviceProviderService;
        this.authenticationFacade = authenticationFacade;
        this.memberAvailabilityRepository = memberAvailabilityRepository;
    }

    public List<Availability> getAllAvailabilitiesEntity() {
        return availabilityRepository.findAll();
    }
    private Member validateMemberExists(Long memberId) {
        return memberService.findbyId(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class, memberId));
    }

    @Transactional
    public void createAvailabilities(AvailabilityRequestNewDTO request) {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        List<MemberAvailability> newEntries = new ArrayList<>();
        for (DaySchedule daySchedule: request.daySchedules()) {
                removeMemberAvailabilities(member.getId(), daySchedule.date());

            for (DateTimeRange timeRange : daySchedule.timeRanges()) {
                List<Availability> list = availabilityRepository.findByStartTimeAndEndTime(timeRange.start(), timeRange.end());

                Availability availability;

                if (list.isEmpty()) {
                    availability = new Availability();
                    availability.setStartTime(timeRange.start());
                    availability.setEndTime(timeRange.end());
                    availability = availabilityRepository.save(availability); // salvar primeiro para garantir ID
                } else {
                    availability = list.get(0); // reutilizar
                }

                MemberAvailability memberAvailability = new MemberAvailability();
                memberAvailability.setMember(member);
                memberAvailability.setAvailability(availability);

                List<LocalDate> dates = new ArrayList<>();
                dates.add(daySchedule.date());
                memberAvailability.setDates(dates);

                member.getMemberAvailabilities().add(memberAvailability);
                newEntries.add(memberAvailability);
            }

        }
        memberAvailabilityRepository.saveAll(newEntries);

    }

    private void removeMemberAvailabilities(Long memberId, LocalDate date) {
        memberAvailabilityRepository.deleteByMemberIdAndDatesStringContaining(memberId, String.valueOf(date));
    }

    public AvailabilityRequestNewDTO getMemberAvailability() {
        Member member = memberService.getMemberByUsername(authenticationFacade.getUsername());
        List<MemberAvailability> availabilities = memberAvailabilityRepository.findByMemberId(member.getId());
        return new AvailabilityRequestNewDTO(availabilityMapper.toDaySchedules(availabilities));
    }
}