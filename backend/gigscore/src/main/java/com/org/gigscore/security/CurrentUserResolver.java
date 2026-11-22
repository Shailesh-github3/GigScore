package com.org.gigscore.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.org.gigscore.entity.User;
import com.org.gigscore.exception.UnauthorizedException;
import com.org.gigscore.repository.UserRepository;
import com.org.gigscore.exception.ResourceNotFoundException;

@Component
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required.");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authentication required."));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}