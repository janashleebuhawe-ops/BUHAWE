package com.ordering.system.config;

import com.ordering.system.entity.User;
import com.ordering.system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Added missing semicolon

@Configuration // This was missing in your snippet
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // SET YOUR PREFERRED CREDENTIALS HERE
            String adminEmail = "admin@gmail.com"; 
            String adminUsername = "buhawe"; 
            String adminPassword = "BUHAWE123";

            // Check by Email to prevent Duplicate Entry errors
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                
                admin.setRole("ROLE_ADMIN");
                admin.setEnabled(true);
                
                userRepository.save(admin);
                System.out.println("✅ SYSTEM: Admin account (" + adminUsername + ") created successfully.");
            } else {
                System.out.println("ℹ️ SYSTEM: Data initialization skipped. Admin email already exists.");
            }
        };
    }
}