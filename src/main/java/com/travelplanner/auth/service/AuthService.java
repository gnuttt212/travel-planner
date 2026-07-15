package com.travelplanner.auth.service;

import com.travelplanner.auth.domain.Role;
import com.travelplanner.auth.domain.User;
import com.travelplanner.auth.dto.AuthRequest;
import com.travelplanner.auth.dto.AuthResponse;
import com.travelplanner.auth.repository.UserRepository;
import com.travelplanner.common.security.CustomUserDetails;
import com.travelplanner.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();
        
        userRepository.save(user);

        var userDetails = new CustomUserDetails(user);
        var jwtToken = jwtUtil.generateToken(userDetails);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
                
        var userDetails = new CustomUserDetails(user);
        var jwtToken = jwtUtil.generateToken(userDetails);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
