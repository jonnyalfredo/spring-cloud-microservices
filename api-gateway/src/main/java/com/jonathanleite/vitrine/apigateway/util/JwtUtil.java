package com.jonathanleite.vitrine.apigateway.util;

import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public Claims validateToken(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("Token expirado");
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new JwtAuthenticationException("Token malformado");
        } catch (JwtException ex) {
            throw new JwtAuthenticationException("Token invalido");
        }
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
