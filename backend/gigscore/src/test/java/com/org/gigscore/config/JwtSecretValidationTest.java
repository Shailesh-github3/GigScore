package com.org.gigscore.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtSecretValidationTest {

    @Test
    void shouldThrowIllegalStateExceptionWhenSecretIsNull() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new JwtUtil(null));
        assertEquals("JWT_SECRET environment variable is missing or shorter than 32 bytes (256 bits required for HMAC-SHA256)", ex.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSecretIsBlank() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new JwtUtil("   "));
        assertEquals("JWT_SECRET environment variable is missing or shorter than 32 bytes (256 bits required for HMAC-SHA256)", ex.getMessage());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenSecretIsTooShort() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new JwtUtil("short-secret-under-32-chars!"));
        assertEquals("JWT_SECRET environment variable is missing or shorter than 32 bytes (256 bits required for HMAC-SHA256)", ex.getMessage());
    }

    @Test
    void shouldSucceedWhenSecretIsAtLeast32Bytes() {
        String validSecret = "this-is-a-valid-32-character-secret-key!!";
        assertDoesNotThrow(() -> new JwtUtil(validSecret));
    }
}
