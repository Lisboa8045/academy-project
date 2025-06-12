package com.academy.config.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareBean implements AuditorAware<String> {

    @Autowired(required = false)
    private HttpServletRequest request;

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(authentication.getName());
        }

        if (isRegisteringAccount()) {
            return Optional.of("user:self");
        }

        return Optional.empty();
    }

    private boolean isRegisteringAccount() {
        if (request == null) return false;
        String path = request.getRequestURI();
        return "POST".equalsIgnoreCase(request.getMethod()) && path.equals("/auth/register");
    }
}