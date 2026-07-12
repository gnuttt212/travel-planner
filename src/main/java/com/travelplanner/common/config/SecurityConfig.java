package com.travelplanner.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cau hinh bao mat co ban.
 *
 * O giai doan khoi dau nay, tat ca endpoint duoc mo (permitAll) de tien
 * phat trien va demo nhanh cac module. Khi trien khai Auth module, chi
 * can:
 *   1. Them JwtAuthenticationFilter va dang ky vao filter chain ben duoi
 *   2. Doi ".anyRequest().permitAll()" thanh cac rule phan quyen cu the
 * Thiet ke tach rieng SecurityConfig ngay tu dau giup viec nang cap khong
 * anh huong toi cac module nghiep vu khac (tuan thu Open/Closed Principle).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless, dung JWT nen khong can CSRF token
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .anyRequest().permitAll() // TODO: sua lai khi co Auth module
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
