package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.availability.AvailabilityMapper;
import com.academy.dtos.availability.AvailabilityRequestNewDTO;
import com.academy.dtos.availability.DateTimeRange;
import com.academy.dtos.availability.DaySchedule;
import com.academy.models.availability.Availability;
import com.academy.models.availability.MemberAvailability;
import com.academy.models.member.Member;
import com.academy.repositories.AvailabilityRepository;
import com.academy.repositories.MemberAvailabilityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final MemberService memberService;
    private final AvailabilityMapper availabilityMapper;
    private final AuthenticationFacade authenticationFacade;
    private final MemberAvailabilityRepository memberAvailabilityRepository;

    @Autowired
    public AvailabilityService(
            AvailabilityRepository availabilityRepository,
            MemberService memberService,
            AvailabilityMapper availabilityMapper,
            AuthenticationFacade authenticationFacade,
            MemberAvailabilityRepository memberAvailabilityRepository) {
        this.availabilityRepository = availabilityRepository;
        this.memberService = memberService;
        this.availabilityMapper = availabilityMapper;
        this.authenticationFacade = authenticationFacade;
        this.memberAvailabilityRepository = memberAvailabilityRepository;
    }

    public List<Availability> getAllAvailabilitiesEntity() {
        return availabilityRepository.findAll();
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