package com.travelplanner.common.security;

import com.travelplanner.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "c2VjcmV0LWtleS1mb3ItdGVzdGluZy1vbmx5LTEyMzQ1Ng==");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 3600000L);
    }

    @Test
    void generateTokenAndValidateTokenForMatchingUser() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("alice")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token, userDetails));
        assertEquals("alice", jwtUtil.extractUsername(token));
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        UserDetails alice = org.springframework.security.core.userdetails.User.withUsername("alice")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        UserDetails bob = org.springframework.security.core.userdetails.User.withUsername("bob")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String token = jwtUtil.generateToken(alice);

        assertFalse(jwtUtil.isTokenValid(token, bob));
    }
}
