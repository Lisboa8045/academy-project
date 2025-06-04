package com.academy.availability;

import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.models.service.Service;
import com.academy.models.service.ServiceTypeEnum;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.repositories.*;
import com.academy.services.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class AvailabilityIntegrationTests {

    @Autowired private AvailabilityService availabilityService;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ServiceProviderService serviceProviderService;
    @Autowired private RoleRepository roleRepository;

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
        return new AvailabilityRequestDTO(null, memberId, day, start, end);
    }

    // Tests

    @Test
    void createAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.MONDAY, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        var response = availabilityService.createAvailability(dto);

        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(defaultMember.getId());
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void getAvailabilitiesByServiceId_shouldReturnResults() {
        Member provider = saveMember("provider1", "PROVIDER");

        Service service = new Service();
        service.setName("Yoga Class");
        service.setServiceType(ServiceTypeEnum.YOGA);
        service.setOwner(provider);
        service = serviceRepository.save(service);

        serviceProviderService.createServiceProvider(
            new ServiceProviderRequestDTO(provider.getId(), service.getId(), List.of(
                ProviderPermissionEnum.READ,
                ProviderPermissionEnum.UPDATE,
                ProviderPermissionEnum.DELETE,
                ProviderPermissionEnum.SERVE
            ))
        );

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

        var updatedDTO = new AvailabilityRequestDTO(
            created.id(),
            dto.memberId(),
            dto.dayOfWeek(),
            dto.startDateTime(),
            dto.endDateTime().plusHours(1)
        );

        var updated = availabilityService.updateAvailability(updatedDTO);

        assertThat(updated.endDateTime()).isEqualTo(updatedDTO.endDateTime());
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

    @Test
    void createAvailability_withNullFields_shouldThrow() {
        assertThatThrownBy(() -> availabilityService.createAvailability(
            new AvailabilityRequestDTO(null, null, null, null, null)))
            .isInstanceOf(InvalidArgumentException.class);
    }
}
