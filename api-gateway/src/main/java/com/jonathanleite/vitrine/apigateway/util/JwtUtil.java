package com.jonathanleite.vitrine.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public Claims validateToken(String token) {

        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature");

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired JWT token");
        }
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }
}