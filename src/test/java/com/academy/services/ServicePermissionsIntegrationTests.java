package com.academy.services;

import com.academy.config.authentication.AuthenticationFacade;
import com.academy.dtos.service.ServiceRequestDTO;
import com.academy.dtos.service.ServiceResponseDTO;
import com.academy.dtos.service_provider.ServiceProviderRequestDTO;
import com.academy.dtos.service_type.ServiceTypeRequestDTO;
import com.academy.dtos.service_type.ServiceTypeResponseDTO;
import com.academy.exceptions.AuthenticationException;
import com.academy.models.ServiceType;
import com.academy.models.member.Member;
import com.academy.models.service.Service;
import com.academy.models.service.service_provider.ProviderPermissionEnum;
import com.academy.models.service.service_provider.ServiceProvider;
import com.academy.repositories.MemberRepository;
import com.academy.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.academy.models.Role;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class ServicePermissionsIntegrationTests {

    private Member memberOwnerOfService;
    private Member memberWithPermissionToUpdateService;
    private Member memberWithPermissionToUpdatePermissions;
    private ServiceType serviceType;

    @Autowired private RoleRepository roleRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ServiceService serviceService;
    @Autowired private ServiceProviderService serviceProviderService;
    @Autowired private ServiceTypeService serviceTypeService;

    @MockBean
    private AuthenticationFacade authenticationFacade; // ✅ MockBean, not Autowired constructor

    @BeforeEach
    public void setup() throws BadRequestException {
        Role role = new Role();
        role.setName("ADMIN");
        roleRepository.save(role);

        memberOwnerOfService = new Member();
        memberOwnerOfService.setUsername("username");
        memberOwnerOfService.setPassword("12Service@");
        memberOwnerOfService.setRole(role);
        memberOwnerOfService.setEmail("username@teste.com");
        memberRepository.save(memberOwnerOfService);

        memberWithPermissionToUpdateService = new Member();
        memberWithPermissionToUpdateService.setUsername("username2");
        memberWithPermissionToUpdateService.setPassword("12Service@");
        memberWithPermissionToUpdateService.setRole(role);
        memberWithPermissionToUpdateService.setEmail("username2@teste.com");
        memberRepository.save(memberWithPermissionToUpdateService);

        memberWithPermissionToUpdatePermissions = new Member();
        memberWithPermissionToUpdatePermissions.setUsername("username3");
        memberWithPermissionToUpdatePermissions.setPassword("12Service@");
        memberWithPermissionToUpdatePermissions.setRole(role);
        memberWithPermissionToUpdatePermissions.setEmail("username3@teste.com");
        memberRepository.save(memberWithPermissionToUpdatePermissions);

        ServiceTypeResponseDTO response = serviceTypeService.create(new ServiceTypeRequestDTO(
                "name",
                "icon"
        ));

        serviceType = serviceTypeService.getEntityById(response.id());

    }

    @Test
    public void testCreateServiceForAuthenticatedUser() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("username");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication); // ✅ Now this works

        ServiceRequestDTO dto = new ServiceRequestDTO(
                "Service",
                "Service",
                20.0,
                1,
                false,
                100,
                serviceType.getName(),
                new ArrayList<>()
        );

        ServiceResponseDTO response = serviceService.create(dto);
        assertNotNull(response);
        assertEquals("Service", response.name());

        Service service = serviceService.getEntityById(response.id());
        assertNotNull(service);
        assertEquals("Service", service.getName());

        ServiceProvider serviceProvider = serviceProviderService.getByServiceIdAndMemberId(response.id(), response.ownerId());
        assertNotNull(serviceProvider);

        List<ProviderPermissionEnum> serviceProviderPermissions = serviceProviderService.getPermissions(serviceProvider.getId());
        assertNotNull(serviceProviderPermissions);

        List<ProviderPermissionEnum> expectedPermissions = Arrays.asList(ProviderPermissionEnum.values());

        assertIterableEquals(expectedPermissions, response.permissions(), "Expected vs Response mismatch");
        assertIterableEquals(serviceProviderPermissions, response.permissions(), "Stored vs Response mismatch");
        assertIterableEquals(serviceProviderPermissions, expectedPermissions, "Stored vs Expected mismatch");
    }

    @Test
    public void testUpdateServiceWithoutServiceProvider() throws BadRequestException {
        Long serviceId = createService();
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("username3");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        assertThrows(AuthenticationException.class, () ->
                serviceService.update(
                        serviceId,
                        new ServiceRequestDTO(
                                "ServiceUpdated",
                                "Service",
                                30.0,
                                1,
                                false,
                                100,
                                serviceType.getName(),
                                new ArrayList<>()
                        )
                )
        );
    }
    @Test
    public void testUpdateServiceWithoutPermission() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdatePermissions.getId(), ProviderPermissionEnum.DELETE);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("username3");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        assertThrows(AuthenticationException.class, () ->
                serviceService.update(
                        serviceId,
                        new ServiceRequestDTO(
                                "ServiceUpdated",
                                "Service",
                                30.0,
                                1,
                                false,
                                100,
                                serviceType.getName(),
                                new ArrayList<>()
                        )
                )
        );
    }

    @Test
    public void testUpdateServiceWithPermission() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdateService.getId(), ProviderPermissionEnum.UPDATE);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("username2");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);
                serviceService.update(
                        serviceId,
                        new ServiceRequestDTO(
                                "ServiceUpdated",
                                "Service",
                                30.0,
                                1,
                                false,
                                100,
                                serviceType.getName(),
                                new ArrayList<>()
                        )
        );
                Service service = serviceService.getEntityById(serviceId);
                assertEquals("ServiceUpdated", service.getName());
                assertEquals(30.0, service.getPrice());
    }

    @Test
    public void testUpdatePermissionsWithoutPermission() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdateService.getId(), ProviderPermissionEnum.UPDATE);
        addServiceProvider(serviceId, memberWithPermissionToUpdatePermissions.getId(), ProviderPermissionEnum.UPDATE);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn(memberWithPermissionToUpdatePermissions.getUsername());
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        assertThrows(AuthenticationException.class, () ->
                serviceService.updateMemberPermissions(
                        serviceId,
                        memberWithPermissionToUpdateService.getId(),
                        List.of(ProviderPermissionEnum.DELETE, ProviderPermissionEnum.READ)
                )
        );
    }
    @Test
    public void testGiveOwnerPermission() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdateService.getId(), ProviderPermissionEnum.UPDATE);
        addServiceProvider(serviceId, memberWithPermissionToUpdatePermissions.getId(), ProviderPermissionEnum.UPDATE_PERMISSIONS);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn(memberWithPermissionToUpdatePermissions.getUsername());
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        assertThrows(BadRequestException.class, () ->
                serviceService.updateMemberPermissions(
                        serviceId,
                        memberWithPermissionToUpdateService.getId(),
                        List.of(ProviderPermissionEnum.OWNER, ProviderPermissionEnum.READ)
                )
        );
    }
    @Test
    public void testUpdateOwnPermissions() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdateService.getId(), ProviderPermissionEnum.UPDATE);
        addServiceProvider(serviceId, memberWithPermissionToUpdatePermissions.getId(), ProviderPermissionEnum.UPDATE_PERMISSIONS);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn(memberWithPermissionToUpdatePermissions.getUsername());
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        assertThrows(BadRequestException.class, () ->
                serviceService.updateMemberPermissions(
                        serviceId,
                        memberWithPermissionToUpdatePermissions.getId(),
                        List.of(ProviderPermissionEnum.OWNER, ProviderPermissionEnum.READ)
                )
        );
    }
    @Test
    public void testUpdateOwnersPermissions() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdateService.getId(), ProviderPermissionEnum.UPDATE);
        addServiceProvider(serviceId, memberWithPermissionToUpdatePermissions.getId(), ProviderPermissionEnum.UPDATE_PERMISSIONS);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn(memberWithPermissionToUpdatePermissions.getUsername());
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);

        assertThrows(BadRequestException.class, () ->
                serviceService.updateMemberPermissions(
                        serviceId,
                        memberOwnerOfService.getId(),
                        List.of(ProviderPermissionEnum.OWNER, ProviderPermissionEnum.READ)
                )
        );
    }
    @Test
    public void testUpdatePermissions() throws BadRequestException {
        Long serviceId = createService();
        addServiceProvider(serviceId, memberWithPermissionToUpdateService.getId(), ProviderPermissionEnum.UPDATE);
        addServiceProvider(serviceId, memberWithPermissionToUpdatePermissions.getId(), ProviderPermissionEnum.UPDATE_PERMISSIONS);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn(memberWithPermissionToUpdatePermissions.getUsername());
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);
                serviceService.updateMemberPermissions(
                        serviceId,
                        memberWithPermissionToUpdateService.getId(),
                        List.of(ProviderPermissionEnum.DELETE, ProviderPermissionEnum.READ));
                List <ProviderPermissionEnum> permissions = serviceProviderService.getPermissionsByProviderIdAndServiceId(memberWithPermissionToUpdateService.getId(), serviceId);
        assertIterableEquals(permissions, List.of(ProviderPermissionEnum.DELETE, ProviderPermissionEnum.READ), "Did not update permissions correctly");

    }


    private Long createService() throws BadRequestException {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("username");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);
        ServiceRequestDTO dto = new ServiceRequestDTO(
                "Service",
                "Service",
                20.0,
                1,
                false,
                100,
                serviceType.getName(),
                new ArrayList<>()
        );
        return serviceService.create(dto).id();
    }
    private void addServiceProvider(Long serviceId, Long memberId, ProviderPermissionEnum permission) throws BadRequestException {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationFacade.getUsername()).thenReturn("username");
        Mockito.when(authenticationFacade.getAuthentication()).thenReturn(authentication);
        serviceProviderService.createServiceProvider(new ServiceProviderRequestDTO(
                memberId,
                serviceId,
                List.of(permission),
                false
        ));

    }


}

