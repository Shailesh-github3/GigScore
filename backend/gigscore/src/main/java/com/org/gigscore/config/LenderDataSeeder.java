package com.org.gigscore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.org.gigscore.entity.User;
import com.org.gigscore.entity.UserRole;
import com.org.gigscore.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LenderDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gigscore.seed-lender.email:}")
    private String seedLenderEmail;

    @Value("${gigscore.seed-lender.password:}")
    private String seedLenderPassword;

    public LenderDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (seedLenderEmail == null || seedLenderEmail.isBlank()
                || seedLenderPassword == null || seedLenderPassword.isBlank()) {
            return;
        }

        String normalizedEmail = seedLenderEmail.trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return;
        }

        User lender = new User();
        lender.setName("Demo Lender");
        lender.setEmail(normalizedEmail);
        lender.setPassword(passwordEncoder.encode(seedLenderPassword));
        lender.setRole(UserRole.LENDER);
        userRepository.save(lender);
        log.info("Seeded demo lender account: {}", normalizedEmail);
    }
}