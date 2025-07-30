package com.academy.appointment;

import com.academy.dtos.appointment.AppointmentRequestDTO;
import com.academy.dtos.appointment.review.ReviewRequestDTO;
import com.academy.dtos.register.RegisterRequestDto;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.tag.TagRequestDTO;
import com.academy.models.Role;
import com.academy.models.member.Member; // --- ADDED (import)
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.models.service.Service;
import com.academy.repositories.RoleRepository;
import com.academy.services.*;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "test-rating")
public class AppointmentRatingTests {

    @Autowired private MemberService memberService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ServiceService serviceService;
    @Autowired private TagService tagService;
    @Autowired private ServiceTypeService serviceTypeService;
    @Autowired private ServiceProviderService serviceProviderService;
    @Autowired private AppointmentService appointmentService;

    private Long serviceProviderId;
    private Long serviceId;

    @BeforeEach
    void setup() {
        // Create roles
        Role ownerRole = new Role();
        ownerRole.setName("OWNER");
        roleRepository.save(ownerRole);

        Role clientRole = new Role();
        clientRole.setName("CLIENT");
        roleRepository.save(clientRole);

        // Register owner and client
        memberService.register(new RegisterRequestDto("test-rating", "Password123!", "owner@example.com", ownerRole.getId(), null, null, null));
        memberService.register(new RegisterRequestDto("client", "Password123!", "client@example.com", clientRole.getId(), null, null, null));
        Long clientId = memberService.getMemberByUsername("client").getId();

        // Create tag and service type
        tagService.create(new TagRequestDTO("test-tag", false, List.of()));
        serviceTypeService.create(new ServiceTypeRequestDTO("test-type", "icon.png"));

        // Create a service
        ServiceRequestDTO serviceDTO = new ServiceRequestDTO(
                "Test Service", "Description", 100, 10, false, 60,
                "test-type", List.of("test-tag")
        );
        try {
            serviceService.create(serviceDTO);
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }

        // Get the provider and service
        serviceProviderId = serviceProviderService.getServiceProviderByUsername("test-rating").getId();
        serviceId = serviceProviderService.getServiceProviderEntityById(serviceProviderId).getService().getId();
    }

    private void createAndReviewAppointment(int rating, int offsetDays) {
        AppointmentRequestDTO dto = new AppointmentRequestDTO(serviceProviderId, LocalDateTime.now().plusDays(1+offsetDays), null, 0, "", null);
        Long appointmentId = appointmentService.createAppointment(dto).id();
        ReviewRequestDTO reviewDTO = new ReviewRequestDTO(rating, "Test review");
        appointmentService.addReview(appointmentId, reviewDTO);
    }

    @Test
    void ratingsAreUpdatedAndRoundedCorrectly() {
        createAndReviewAppointment(4,1);
        createAndReviewAppointment(5,2);
        createAndReviewAppointment(3, 3); // Avg = 4.0

        ServiceProvider provider = serviceProviderService.getServiceProviderEntityById(serviceProviderId);
        Service service = serviceService.getServiceEntityById(serviceId);

        Member providerMember = memberService.getMemberByUsername("test-rating"); // --- ADDED

        assertThat(provider.getRating()).isEqualTo(4);
        assertThat(service.getRating()).isEqualTo(4);
        assertThat(providerMember.getRating()).isEqualTo(4); // --- ADDED
    }

    @Test
    void roundingIsCorrectlyHandledAboveHalf() {
        createAndReviewAppointment(4,1);
        createAndReviewAppointment(5,2); // Avg = 4.5 → should round to 5.0

        ServiceProvider provider = serviceProviderService.getServiceProviderEntityById(serviceProviderId);
        Service service = serviceService.getServiceEntityById(serviceId);

        Member providerMember = memberService.getMemberByUsername("test-rating"); // --- ADDED

        assertThat(provider.getRating()).isEqualTo(5);
        assertThat(service.getRating()).isEqualTo(5);
        assertThat(providerMember.getRating()).isEqualTo(5); // --- ADDED
    }

    @Test
    void roundingIsCorrectlyHandledBelowHalf() {
        createAndReviewAppointment(4,1);
        createAndReviewAppointment(3,2); // Avg = 3.5 → should round to 4.0

        ServiceProvider provider = serviceProviderService.getServiceProviderEntityById(serviceProviderId);
        Service service = serviceService.getServiceEntityById(serviceId);

        Member providerMember = memberService.getMemberByUsername("test-rating"); // --- ADDED

        assertThat(provider.getRating()).isEqualTo(4);
        assertThat(service.getRating()).isEqualTo(4);
        assertThat(providerMember.getRating()).isEqualTo(4); // --- ADDED
    }
}
