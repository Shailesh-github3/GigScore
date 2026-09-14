package com.org.gigscore.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.org.gigscore.security.ErrorResponseWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<SimpleGrantedAuthority> EMPTY_AUTHORITIES = Collections.emptyList();

    private final JwtUtil jwtUtil;
    private final ErrorResponseWriter errorResponseWriter;

    public JwtAuthFilter(JwtUtil jwtUtil, ErrorResponseWriter errorResponseWriter) {
        this.jwtUtil = jwtUtil;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateJwtToken(token)) {
            errorResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtUtil.extractEmail(token);

            if (email != null && !email.isBlank()) {
                String role = jwtUtil.extractRole(token);
                List<SimpleGrantedAuthority> authorities = role == null || role.isBlank()
                        ? EMPTY_AUTHORITIES
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
