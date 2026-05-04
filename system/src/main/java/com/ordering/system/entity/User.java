package com.ordering.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails { // ✅ Added UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role;

    private boolean enabled = false;

    @Column(unique = true)
    private String verificationToken;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "systemAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private Labor laborProfile;

    // --- Spring Security Methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Prefixes your role (e.g., "ADMIN") with "ROLE_" so Spring recognizes it
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return this.enabled; }
}