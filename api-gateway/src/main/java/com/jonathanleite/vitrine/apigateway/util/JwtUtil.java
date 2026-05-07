package com.jonathanleite.vitrine.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Valida o token JWT e retorna os claims
     */
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

    /**
     * Extrai o username (subject) do token
     */
    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }
}