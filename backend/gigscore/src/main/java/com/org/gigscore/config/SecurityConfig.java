package com.org.gigscore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.org.gigscore.security.ErrorResponseWriter;
import com.org.gigscore.security.RateLimitFilter;
import com.org.gigscore.security.RequestLoggingFilter;

@Configuration
public class SecurityConfig {
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "Authentication required.";
    private static final String ACCESS_DENIED_MESSAGE = "Access denied.";

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final ErrorResponseWriter errorResponseWriter;
    private final org.springframework.core.env.Environment environment;

    @org.springframework.beans.factory.annotation.Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerEnabled;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter,
                          RequestLoggingFilter requestLoggingFilter, ErrorResponseWriter errorResponseWriter,
                          org.springframework.core.env.Environment environment) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.requestLoggingFilter = requestLoggingFilter;
        this.errorResponseWriter = errorResponseWriter;
        this.environment = environment;
    }

    private boolean isSwaggerExposed() {
        return swaggerEnabled || environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev", "local"));
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                errorResponseWriter.write(response, 401, AUTHENTICATION_REQUIRED_MESSAGE);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                errorResponseWriter.write(response, 403, ACCESS_DENIED_MESSAGE);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean allowSwagger = isSwaggerExposed();
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler()))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/v1/users/login", "/error").permitAll()
                    .requestMatchers("/api/v1/users").permitAll();
                if (allowSwagger) {
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll();
                }
                auth.requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/v1/lender/**").hasRole("LENDER")
                    .anyRequest().authenticated();
            })
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, JwtAuthFilter.class)
            .addFilterBefore(requestLoggingFilter, RateLimitFilter.class);

        return http.build();
    }
}