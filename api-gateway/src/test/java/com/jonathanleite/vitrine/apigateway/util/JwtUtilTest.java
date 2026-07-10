package com.jonathanleite.vitrine.apigateway.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    @Test
    void shouldRejectMissingJwtSecret() {
        JwtUtil jwtUtil = new JwtUtil();

        assertThatThrownBy(jwtUtil::validateSecretConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET deve ser configurado por variavel de ambiente");
    }

    @Test
    void shouldRejectBlankJwtSecret() {
        JwtUtil jwtUtil = jwtUtilWithSecret(" ");

        assertThatThrownBy(jwtUtil::validateSecretConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET deve ser configurado por variavel de ambiente");
    }

    @Test
    void shouldRejectShortJwtSecret() {
        JwtUtil jwtUtil = jwtUtilWithSecret("short-secret");

        assertThatThrownBy(jwtUtil::validateSecretConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET deve possuir pelo menos 32 caracteres");
    }

    @Test
    void shouldAcceptConfiguredJwtSecret() {
        JwtUtil jwtUtil = jwtUtilWithSecret("test-secret-with-at-least-32-characters");

        assertThatCode(jwtUtil::validateSecretConfiguration)
                .doesNotThrowAnyException();
    }

    private JwtUtil jwtUtilWithSecret(String secret) {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        return jwtUtil;
    }
}
