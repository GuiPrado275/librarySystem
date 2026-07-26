package com.guilherme.librarySystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

@Component
public class JWTUtil {

    @Value("${jwt.secret}") //from application.properties
    private String secret;

    @Value("${jwt.expiration}") //from application.properties
    private Long expiration; //must be a number type: it's added to a long (currentTimeMillis) below

    public String generateToken(String email) { //token that will later be used to authenticate the user
        SecretKey key = getKeyBySecret();
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + this.expiration))
                .signWith(key)
                .compact();
    }

    private SecretKey getKeyBySecret() { //encryption key
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return key;
    }

    public boolean isValidToken(String token) { //checks whether the token is valid (not expired)
        Claims claims = getClaims(token);
        if (Objects.nonNull(claims)) {
            String email = claims.getSubject();
            Date expirationDate = claims.getExpiration();
            Date now = new Date(System.currentTimeMillis());
            if (Objects.nonNull(email) && Objects.nonNull(expirationDate) && now.before(expirationDate)) {
                return true;
            }
        }
        return false; //null/malformed claims, or the token has already expired
    }

    public String getEmail(String token) { //get the email out of the token
        Claims claims = getClaims(token);
        if (Objects.nonNull(claims)) {
            return claims.getSubject();
        }
        return null;
    }

    public Claims getClaims(String token) { //generate the token's claims
        SecretKey key = getKeyBySecret();
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null; //if the token is null or invalid, return this
        }
    }

}