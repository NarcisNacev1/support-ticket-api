package com.example.support_ticket_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    @Value("${api.security.key}")
    private String apiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        logger.info("=== API Key Filter Check ===");
        logger.info("Request URI: {}", path);
        logger.info("Request Method: {}", request.getMethod());

        boolean shouldSkip = path.startsWith("/v3/api-docs") ||
                           path.startsWith("/swagger-ui") ||
                           path.equals("/swagger-ui.html");

        logger.info("Should skip filter: {}", shouldSkip);
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
        throws ServletException, IOException {

        logger.info("=== API Key Authentication ===");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("Request Method: {}", request.getMethod());

        logger.info("=== Request Headers ===");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            logger.info("Header: {} = {}", headerName, headerValue);
        }

        String requestKey = request.getHeader("X-API-KEY");
        logger.info("X-API-KEY from request: '{}'", requestKey);
        logger.info("Expected API key: '{}'", apiKey);
        logger.info("Keys match: {}", (requestKey != null && requestKey.equals(apiKey)));

        if (requestKey == null || !requestKey.equals(apiKey)) {
            logger.error("API Key validation failed! Request key: '{}', Expected: '{}'", requestKey, apiKey);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API Key");
            return;
        }

        logger.info("API Key validation successful!");

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                "api-user",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_USER"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("Authentication set in SecurityContext");

        filterChain.doFilter(request, response);
    }
}