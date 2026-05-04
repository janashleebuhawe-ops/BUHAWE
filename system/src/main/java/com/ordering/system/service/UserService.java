package com.ordering.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ordering.system.entity.User;
import com.ordering.system.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
	

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${abstract.api.key}")
    private String apiKey;

    @Value("${abstract.api.url}")
    private String apiUrl;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Standard Registration Flow:
     * 1. Check if email exists in the real world (API)
     * 2. Check if email is already in our MySQL (Database)
     * 3. Save as 'disabled' and send verification email
     */
 // ... inside UserService.java

    public void saveUser(User user) {
        // 1. Verify if the email is physically real via API
        if (!isEmailPhysicallyReal(user.getEmail())) {
            throw new RuntimeException("INVALID_INBOX");
        }

        // 2. Check if already in DB
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("EMAIL_TAKEN");
        }
        
    
        // 2. Prep
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 3. Save
        userRepository.save(user);

        // 4. Try Email
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        } catch (Exception e) {
            System.err.println("CRITICAL: Email failed but user was saved. Error: " + e.getMessage());
        }
    }

    /**
     * Updates existing user. Only checks email uniqueness if it's actually changed.
     */
    public void updateUser(User user, String newPassword) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only check EMAIL_TAKEN if the user is trying to change their email
        if (!existing.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(user.getEmail()) != null) {
                throw new RuntimeException("EMAIL_TAKEN");
            }
            existing.setEmail(user.getEmail());
        }

        existing.setRole(user.getRole());

        if (newPassword != null && !newPassword.isBlank()) {
            existing.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(existing);
    }

 // ... inside UserService.java

 // Inside UserService.java
    @Transactional // ✅ Forces the database to commit changes immediately
    public User verifyToken(String token) {
        User user = userRepository.findByVerificationToken(token);
        
        if (user != null) {
            user.setEnabled(true); // ✅ Set to active
            user.setVerificationToken(null); // ✅ Clear token so it can't be used twice
            return userRepository.saveAndFlush(user); // ✅ Flush to disk immediately
        }
        return null;
    }

    
    
    
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Calls AbstractAPI to verify email deliverability and quality.
     */
    public boolean isEmailPhysicallyReal(String email) {
        String url = apiUrl + "?api_key=" + apiKey + "&email=" + email;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return true;

            Map<String, Object> deliverability = (Map<String, Object>) response.get("email_deliverability");
            Map<String, Object> quality = (Map<String, Object>) response.get("email_quality");

            String status = (String) deliverability.get("status");
            
            // Safe number parsing (handles both 1 and 0.95 correctly)
            Number scoreNum = (Number) quality.get("score");
            double score = (scoreNum != null) ? scoreNum.doubleValue() : 0.0;

            return "deliverable".equalsIgnoreCase(status) && score >= 0.50;
        } catch (Exception e) {
            // Fail-safe: if API is down, don't block the user
            return true; 
        }
    }
    
}