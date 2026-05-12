package com.municipal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ✅ Disable CSRF (important for REST + fetch)
                .csrf(csrf -> csrf.disable())
                // ✅ Allow requests
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/raise.html",
                        "/track.html",
                        "/staff-login.html",
                        "/staff-register.html",
                        "/dashboard.html",
                        "/style.css",
                        "/script.js",
                        "/images/**"
                ).permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
                )
                // ❌ Disable default login page
                .formLogin(form -> form.disable());

        return http.build();
    }
}
