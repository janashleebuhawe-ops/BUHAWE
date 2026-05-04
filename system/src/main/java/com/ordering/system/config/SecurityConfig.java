package com.ordering.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 1. PUBLIC ACCESS
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/login", "/register",
                    "/users/verify"
                ).permitAll()

                // 2. ADMIN ONLY
                .requestMatchers("/users/**").hasRole("ADMIN")

                // 3. ITEMS & LABOR — ADMIN and CO-ADMIN only
                .requestMatchers(HttpMethod.POST, "/items/**", "/labor/**").hasAnyRole("ADMIN", "CO-ADMIN")
                .requestMatchers(HttpMethod.PUT, "/items/**", "/labor/**").hasAnyRole("ADMIN", "CO-ADMIN")
                .requestMatchers("/items/delete/**", "/labor/delete/**").hasAnyRole("ADMIN", "CO-ADMIN")

                // 4. ORDERS — ADMIN, CO-ADMIN, and STAFF can place orders
                //    but only ADMIN and CO-ADMIN can delete/cancel orders
                .requestMatchers(HttpMethod.POST, "/orders/add").hasAnyRole("ADMIN", "CO-ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/orders/**").hasAnyRole("ADMIN", "CO-ADMIN")
                .requestMatchers(HttpMethod.PUT, "/orders/**").hasAnyRole("ADMIN", "CO-ADMIN")
                .requestMatchers("/orders/delete/**").hasAnyRole("ADMIN", "CO-ADMIN")
                .requestMatchers("/orders/**").hasAnyRole("ADMIN", "CO-ADMIN", "STAFF")

                // 5. GENERAL PAGES — all authenticated roles
                .requestMatchers("/dashboard/**", "/menu/**").hasAnyRole("ADMIN", "CO-ADMIN", "STAFF")

                // 6. EVERYTHING ELSE — must be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/menu", true)
                .failureHandler((request, response, exception) -> {
                    if (exception.getMessage().toLowerCase().contains("disabled")) {
                        response.sendRedirect("/login?disabled");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}