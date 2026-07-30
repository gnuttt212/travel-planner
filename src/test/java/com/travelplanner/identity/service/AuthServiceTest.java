package com.travelplanner.identity.service;

import com.travelplanner.common.security.CustomUserDetails;
import com.travelplanner.common.security.JwtUtil;
import com.travelplanner.identity.domain.Role;
import com.travelplanner.identity.domain.User;
import com.travelplanner.identity.dto.AuthRequest;
import com.travelplanner.identity.dto.AuthResponse;
import com.travelplanner.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void registerShouldPersistUserWithUserRoleAndReturnToken() {
        AuthRequest request = new AuthRequest("bob@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(jwtUtil.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void loginShouldAuthenticateAndReturnJwtToken() {
        AuthRequest request = new AuthRequest("jane@example.com", "secret123");
        User storedUser = User.builder()
                .email(request.getEmail())
                .password("encoded-secret")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(storedUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(storedUser.getEmail(), storedUser.getPassword(), List.of()));
        when(jwtUtil.generateToken(any(CustomUserDetails.class))).thenReturn("login-token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("login-token");
    }

    @Test
    void registerShouldFailWhenEmailAlreadyExists() {
        AuthRequest request = new AuthRequest("bob@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }
}
