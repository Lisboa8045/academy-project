package com.academy.availability;

import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.Member;
import com.academy.models.Role;
import com.academy.models.service.Service;
import com.academy.models.ServiceType;
import com.academy.models.service.ServiceTypeEnum;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.repositories.*;
import com.academy.services.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.academy.models.service.ServiceTypeEnum;

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

    

    private Member defaultMember;

    @BeforeEach
    void setUp() {
        defaultMember = saveMember("testuser", "CLIENT");
    }

    /* @AfterEach
    void tearDown() {
        availabilityRepository.deleteAll();        
        serviceProviderRepository.deleteAll();     
        serviceRepository.deleteAll();             
        memberRepository.deleteAll();              
    }
 */

    // Helper methods
    private Member saveMember(String username, String roleName) {
        Role role = new Role();
        role.setId(1);
        role.setName(roleName);

        Member member = new Member();
        member.setUsername(username);
        member.setPassword("password");
        member.setEmail(username + "@email.com");
        member.setRole(role);

        return memberRepository.save(member);
    }


    private AvailabilityRequestDTO createDTO(Long memberId, DayOfWeek day, LocalDateTime start, LocalDateTime end) {
        AvailabilityRequestDTO dto = new AvailabilityRequestDTO();
        dto.setMemberId(memberId);
        dto.setDayOfWeek(day);
        dto.setStartDateTime(start);
        dto.setEndDateTime(end);
        return dto;
    }

    // Tests

    @Test
    void createAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.MONDAY, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        var response = availabilityService.createAvailability(dto);

        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(defaultMember.getId());
        assertThat(response.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void getAvailabilitiesByServiceId_shouldReturnResults() {
        Member provider = saveMember("provider1", "PROVIDER");
        provider = memberRepository.save(provider); // <- garantir que está persistido

        Service service = new Service();
        service.setName("Yoga Class");
        service.setServiceType(ServiceTypeEnum.YOGA);
        service.setOwner(provider); // agora sim, está persistido
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
        assertThat(availabilities.get(0).getMemberId()).isEqualTo(provider.getId());
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
        assertThat(list.get(0).getMemberId()).isEqualTo(defaultMember.getId());
    }

    @Test
    void updateAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.FRIDAY, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(2));
        var created = availabilityService.createAvailability(dto);

        dto.setId(created.getId());
        dto.setEndDateTime(dto.getEndDateTime().plusHours(1));
        var updated = availabilityService.updateAvailability(dto);

        assertThat(updated.getEndDateTime()).isEqualTo(dto.getEndDateTime());
    }

    @Test
    void deleteAvailability_shouldSucceed() {
        var dto = createDTO(defaultMember.getId(), DayOfWeek.SATURDAY, LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(6).plusHours(2));
        var created = availabilityService.createAvailability(dto);

        availabilityService.deleteAvailabilityById(created.getId());
        assertThat(availabilityRepository.findById(created.getId())).isEmpty();
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
        assertThat(response.getMemberId()).isEqualTo(anotherMember.getId());
    }

    @Test
    void createAvailability_withNullFields_shouldThrow() {
        assertThatThrownBy(() -> availabilityService.createAvailability(new AvailabilityRequestDTO()))
            .isInstanceOf(InvalidArgumentException.class);
    }
}
