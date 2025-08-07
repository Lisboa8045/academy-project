package com.academy.config.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareBean implements AuditorAware<String> {

    @Autowired(required = false)
    private HttpServletRequest request;

    private final AuthenticationFacade authenticationFacade;

    // System auditor constant
    private static final String SYSTEM_AUDITOR = "system";

    public AuditorAwareBean(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        // 1. First try to get authenticated user from security context
        Optional<String> authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.isPresent()) {
            return authenticatedUser;
        }

        // 2. Check for registration case (existing functionality)
        if (isRegisteringAccount()) {
            return Optional.of("user:self");
        }

        // 3. New: Return system auditor when no request context exists
        if (request == null) {
            return Optional.of(SYSTEM_AUDITOR);
        }

        // 4. Fallback to empty (existing behavior)
        return Optional.empty();
    }

    private Optional<String> getAuthenticatedUser() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(authentication.getName());
        }
        return Optional.empty();
    }

    private boolean isRegisteringAccount() {
        if (request == null) return false;
        try {
            String path = request.getRequestURI();
            return "POST".equalsIgnoreCase(request.getMethod()) &&
                    path.equals("/auth/register");
        } catch (IllegalStateException e) {
            // Handle case where request is no longer active
            return false;
        }
    }
}