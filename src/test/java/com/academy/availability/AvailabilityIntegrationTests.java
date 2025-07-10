package com.academy.availability;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.availability.AvailabilityRequestDTO;
import com.academy.dtos.availability.AvailabilityResponseDTO;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.models.member.Member;
import com.academy.models.Role;
import com.academy.models.service.Service;
import com.academy.repositories.RoleRepository;
import com.academy.services.AvailabilityService;
import com.academy.services.MemberService;
import com.academy.services.ServiceService;
import com.academy.services.ServiceTypeService;
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
    @Autowired private ServiceService serviceService;
    @Autowired private ServiceTypeService serviceTypeService;
    @Autowired private RoleRepository roleRepository;

    @MockBean
    private AuthenticationFacade authenticationFacade; // ✅ MockBean, not Autowired constructor

    private Member defaultMember;
    private Role defaultRole;
    @Autowired
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        defaultRole = new Role();
        defaultRole.setName("USER");
        defaultRole = roleRepository.save(defaultRole);
        defaultMember = saveMember("testuser", "CLIENT");
    }

    private Member saveMember(String username, String roleName) {

        Role role = new Role();
        role.setName(roleName);
        Role savedRole = roleRepository.save(role);

        RegisterRequestDto registerRequest = new RegisterRequestDto(
                username,
                "Password1!",
                username + "@email.com",
                savedRole.getId(),
                "Rua Teste 1",
                "1000-100",
                "912345678"
        );

        long id = memberService.register(registerRequest);
        return memberService.getMemberEntityById(id);
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

        ServiceTypeRequestDTO serviceTypeRequestDTO = new ServiceTypeRequestDTO("Basic Type", "type.png");
        serviceTypeService.create(serviceTypeRequestDTO);

        Service service = serviceService.createToEntity(new ServiceRequestDTO(
                "Basic Service",
                "Service",
                20.0,
                1,
                false,
                100,
                serviceTypeRequestDTO.name(),
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
        var dto = createDTO(
                defaultMember.getId(),
                DayOfWeek.SATURDAY,
                LocalDateTime.of(2025, 1, 1, 10, 0).plusDays(5),
                LocalDateTime.of(2025, 1, 1, 10, 0).plusDays(5).plusHours(2));
        var created = availabilityService.createAvailability(dto);

        // Verify it exists
        assertThat(availabilityService.getAvailabilityById(created.id())).isPresent();

        // Delete it
        availabilityService.deleteAvailabilityById(created.id());

        // Verify deletion - now expects empty Optional
        assertThat(availabilityService.getAvailabilityById(created.id())).isEmpty();
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
