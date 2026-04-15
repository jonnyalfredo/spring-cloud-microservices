package com.jonathanleite.vitrine.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class JwtUtil {

    private static final String SECRET = "my-secret-key";

    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();

        } catch (SignatureException e) {
            throw new RuntimeException("Token inválido");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar token");
        }
    }
}