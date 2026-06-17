package com.example.back_end.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.back_end.utils.JwtUtils;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private static final String SECRET = "test-jwt-secret-key-for-unit-testing-32b";

    private JwtUtils createJwtUtils() {
        return new JwtUtils(SECRET);
    }

    @Test
    void constructor_shouldThrowWhenSecretIsNull() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils(null));
    }

    @Test
    void constructor_shouldThrowWhenSecretIsBlank() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils("   "));
    }

    @Test
    void constructor_shouldPadShortSecret() {
        JwtUtils jwt = new JwtUtils("short");
        assertNotNull(jwt);
    }

    @Test
    void generateToken_shouldReturnNonNull() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateToken(1L, "test@example.com");
        assertNotNull(token);
    }

    @Test
    void generateRefreshToken_shouldReturnNonNull() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateRefreshToken(1L, "test@example.com");
        assertNotNull(token);
    }

    @Test
    void getEmailFromToken_shouldReturnCorrectEmail() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateToken(42L, "user@test.com");
        assertEquals("user@test.com", jwt.getEmailFromToken(token));
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateToken(42L, "user@test.com");
        assertEquals(42L, jwt.getUserIdFromToken(token));
    }

    @Test
    void validateToken_shouldAcceptAccessToken() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateToken(1L, "a@b.com");
        assertTrue(jwt.validateToken(token));
    }

    @Test
    void validateToken_shouldRejectRefreshToken() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateRefreshToken(1L, "a@b.com");
        assertFalse(jwt.validateToken(token));
    }

    @Test
    void validateRefreshToken_shouldAcceptRefreshToken() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateRefreshToken(1L, "a@b.com");
        assertTrue(jwt.validateRefreshToken(token));
    }

    @Test
    void validateRefreshToken_shouldRejectAccessToken() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateToken(1L, "a@b.com");
        assertFalse(jwt.validateRefreshToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        JwtUtils jwt = createJwtUtils();
        assertFalse(jwt.validateToken("not.a.valid.token"));
    }

    @Test
    void getExpiration_shouldReturnFutureDate() {
        JwtUtils jwt = createJwtUtils();
        String token = jwt.generateToken(1L, "a@b.com");
        assertTrue(jwt.getExpiration(token).getTime() > System.currentTimeMillis());
    }
}
