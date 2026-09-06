package com.wallet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wallet.entity.User;
import com.wallet.enums.Role;

class JwtServiceTest {

    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {

        String secret = "my-super-secret-key-for-wallet-system-123456789";

        long expirationTime = 15 * 60 * 1000; // 15 minutes

        jwtService = new JwtService(secret, expirationTime);

        user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("password123")
                .role(Role.USER)
                .build();
    }

    // 1. TOKEN GENERATION

    @Test
    void generateToken_shouldReturnToken() {

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    // 2. TOKEN SHOULD CONTAIN USER EMAIL

    @Test
    void extractEmail_shouldReturnUserEmail() {

        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertEquals("test@gmail.com", email);
    }

    // 3. TOKEN SHOULD BE VALID

    @Test
    void isTokenValid_shouldReturnTrue_forCorrectUser() {

        String token = jwtService.generateToken(user);

        boolean result = jwtService.isTokenValid(token, user);

        assertTrue(result);
    }

    // 4. TOKEN SHOULD BE INVALID FOR DIFFERENT USER

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUser() {

        String token = jwtService.generateToken(user);

        User differentUser = User.builder()
                .id(2L)
                .email("other@gmail.com")
                .password("password123")
                .role(Role.USER)
                .build();

        boolean result =
                jwtService.isTokenValid(token, differentUser);

        assertFalse(result);
    }

    // 5. TOKEN SHOULD BE INVALID WHEN EMAIL IS DIFFERENT

    @Test
    void isTokenValid_shouldReturnFalse_whenEmailIsDifferent() {

        String token = jwtService.generateToken(user);

        User anotherUser = User.builder()
                .id(1L)
                .email("different@gmail.com")
                .password("password123")
                .role(Role.USER)
                .build();

        boolean result =
                jwtService.isTokenValid(token, anotherUser);

        assertFalse(result);
    }

    // 6. TOKEN SHOULD CONTAIN EXPIRATION

    @Test
    void generatedToken_shouldContainExpiration() {

        String token = jwtService.generateToken(user);

        assertNotNull(token);

        // If token can be validated, expiration is present
        // and currently valid.
        assertTrue(jwtService.isTokenValid(token, user));
    }

    // 7. INVALID TOKEN SHOULD NOT BE VALID

    @Test
    void isTokenValid_shouldThrowException_forInvalidToken() {

        String invalidToken = "invalid.jwt.token";

        assertThrows(
                Exception.class,
                () -> jwtService.isTokenValid(invalidToken, user)
        );
    }
}