package com.creditapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    private final String defaultTargetUrl;

    public CustomAuthenticationSuccessHandler() {
        this.defaultTargetUrl = "/dashboard";
    }

    public CustomAuthenticationSuccessHandler(String defaultTargetUrl) {
        this.defaultTargetUrl = defaultTargetUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        try {
            log.info("=== AUTHENTICATION SUCCESS HANDLER INVOKED ===");
            log.info("User: {}, authorities: {}, redirecting to: {}", 
                    authentication.getName(), authentication.getAuthorities(), defaultTargetUrl);
            
            // Force session creation
            var session = request.getSession(true);
            log.info("Session created: ID={}, isNew={}, creationTime={}, lastAccessedTime={}", 
                    session.getId(), session.isNew(), session.getCreationTime(), session.getLastAccessedTime());
            
            // Log all cookies
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    log.info("Cookie: {}={}", cookie.getName(), cookie.getValue());
                }
            }
            
            log.info("Redirecting to: {}", defaultTargetUrl);
            response.sendRedirect(defaultTargetUrl);
            log.info("=== REDIRECT SENT ===");
        } catch (Exception e) {
            log.error("Error in authentication success handler", e);
            throw e;
        }
    }
}
