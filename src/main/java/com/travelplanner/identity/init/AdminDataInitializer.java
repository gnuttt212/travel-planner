package com.travelplanner.identity.init;

import com.travelplanner.identity.domain.Role;
import com.travelplanner.identity.domain.User;
import com.travelplanner.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@localhost}")
    private String adminEmail;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .displayName("Administrator")
                .avatarUrl(null)
                .bio("Default admin account")
                .role(Role.ROLE_ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Seeded admin user: {} (password set from app properties)", adminEmail);
    }
}
