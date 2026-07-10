package com.jonathanleite.vitrine.apigateway.security;

import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import com.jonathanleite.vitrine.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthenticationService {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String authenticate(String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);
            String username = jwtUtil.extractUsername(claims);

            if (username == null || username.isBlank()) {
                throw new JwtAuthenticationException("Token sem identificacao do usuario");
            }

            return username;
        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Token invalido");
        }
    }
}
