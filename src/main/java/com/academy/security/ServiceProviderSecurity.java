package com.academy.security;

import com.academy.services.MemberService;
import com.academy.services.ServiceProviderService;
import org.springframework.stereotype.Component;

@Component
public class ServiceProviderSecurity {

    private final ServiceProviderService serviceProviderService;
    private final MemberService memberService;

    public ServiceProviderSecurity(ServiceProviderService spService, MemberService memberService) {
        this.serviceProviderService = spService;
        this.memberService = memberService;
    }

    public boolean isOwner(Long serviceProviderId, String username) {
        return true;
        //TODO Implement
    }
}
