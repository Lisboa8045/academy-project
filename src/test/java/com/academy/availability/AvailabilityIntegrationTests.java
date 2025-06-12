package com.academy.availability;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.dtos.service.ServiceMapper;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.models.ServiceType;
import com.academy.models.service.Service;
import com.academy.repositories.AvailabilityRepository;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import com.academy.repositories.ServiceRepository;
import com.academy.repositories.ServiceTypeRepository;
import com.academy.services.AvailabilityService;
import com.academy.services.ServiceProviderService;
import com.academy.services.ServiceService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class AvailabilityIntegrationTests {

    @Autowired private AvailabilityService availabilityService;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ServiceService serviceService;
    @Autowired private ServiceMapper serviceMapper;
    @Autowired private ServiceTypeRepository serviceTypeRepository;
    @Autowired private ServiceProviderService serviceProviderService;
    @Autowired private RoleRepository roleRepository;

    @MockBean
    private AuthenticationFacade authenticationFacade; // ✅ MockBean, not Autowired constructor

    private Member defaultMember;

    @BeforeEach
    void setUp() {
        defaultMember = saveMember("testuser", "CLIENT");
    }

    private Member saveMember(String username, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        Role savedRole = roleRepository.save(role);

        Member member = new Member();
        member.setUsername(username);
        member.setPassword("password");
        member.setEmail(username + "@email.com");
        member.setRole(savedRole);

        return memberRepository.save(member);
    }

    private AvailabilityRequestDTO createDTO(Long memberId, DayOfWeek day, LocalDateTime start, LocalDateTime end) {
        return new AvailabilityRequestDTO(memberId, day, start, end);
    }

    @Test
    void createAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.MONDAY, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        var response = availabilityService.createAvailability(dto);

        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(defaultMember.getId());
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void getAvailabilitiesByServiceId_shouldReturnResults() throws BadRequestException {
        Member provider = saveMember("provider1", "PROVIDER");
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("provider1");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        ServiceType type = new ServiceType();
        type.setName("Basic Type");
        type.setIcon("type.png");
        serviceTypeRepository.save(type);

        Service service = serviceService.createToEntity(new ServiceRequestDTO(
                "Basic Service",
                "Service",
                20.0,
                1,
                false,
                100,
                type.getName(),
                new ArrayList<>()
        ));

        availabilityService.createAvailability(
            createDTO(provider.getId(), DayOfWeek.MONDAY, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2))
        );

        List<AvailabilityResponseDTO> availabilities = availabilityService.getAvailabilitiesByServiceId(service.getId());

        assertThat(availabilities).isNotEmpty();
        assertThat(availabilities.get(0).memberId()).isEqualTo(provider.getId());
    }

    @Test
    void createAvailability_withDuplicateTimes_shouldThrow() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.TUESDAY, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2));
        availabilityService.createAvailability(dto);

        assertThatThrownBy(() -> availabilityService.createAvailability(dto))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void getAllAvailabilities_shouldReturnList() {
        availabilityService.createAvailability(
            createDTO(defaultMember.getId(), DayOfWeek.WEDNESDAY, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(2))
        );

        var all = availabilityService.getAllAvailabilities();
        assertThat(all).isNotEmpty();
    }

    @Test
    void getAvailabilitiesByMemberId_shouldReturnMemberAvailabilities() {
        availabilityService.createAvailability(
            createDTO(defaultMember.getId(), DayOfWeek.THURSDAY, LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(2))
        );

        var list = availabilityService.getAvailabilitiesByMemberId(defaultMember.getId());
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).memberId()).isEqualTo(defaultMember.getId());
    }

    @Test
    void updateAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.FRIDAY, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(2));
        var created = availabilityService.createAvailability(dto);

        var updatedDto = new AvailabilityRequestDTO(
            created.memberId(),
            created.dayOfWeek(),
            created.startDateTime(),
            created.endDateTime().plusHours(1)
        );

        var updated = availabilityService.updateAvailability(created.id(), updatedDto);
        assertThat(updated.endDateTime()).isEqualTo(updatedDto.endDateTime());
    }

    @Test
    void deleteAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.SATURDAY, LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(6).plusHours(2));
        var created = availabilityService.createAvailability(dto);

        availabilityService.deleteAvailabilityById(created.id());
        assertThat(availabilityRepository.findById(created.id())).isEmpty();
    }

    @Test
    void deleteAvailability_nonExistent_shouldThrow() {
        assertThatThrownBy(() -> availabilityService.deleteAvailabilityById(99999L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createAvailability_withInvalidRange_shouldThrow() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.MONDAY, LocalDateTime.now().plusDays(1).plusHours(2), LocalDateTime.now().plusDays(1));
        assertThatThrownBy(() -> availabilityService.createAvailability(dto))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void createAvailability_overlappingSameMember_shouldThrow() {
        var start = LocalDateTime.now().plusDays(1);
        var end = start.plusHours(2);

        availabilityService.createAvailability(createDTO(defaultMember.getId(), DayOfWeek.MONDAY, start, end));
        assertThatThrownBy(() ->
            availabilityService.createAvailability(createDTO(defaultMember.getId(), DayOfWeek.MONDAY, start.plusMinutes(30), end.plusMinutes(30)))
        ).isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void createAvailability_overlappingDifferentMembers_shouldSucceed() {
        var anotherMember = saveMember("anotheruser", "CLIENT");

        var start = LocalDateTime.now().plusDays(1);
        var end = start.plusHours(2);

        availabilityService.createAvailability(createDTO(defaultMember.getId(), DayOfWeek.MONDAY, start, end));
        var dto = createDTO(anotherMember.getId(), DayOfWeek.MONDAY, start.plusMinutes(30), end.plusMinutes(30));

        var response = availabilityService.createAvailability(dto);

        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(anotherMember.getId());
    }
}
