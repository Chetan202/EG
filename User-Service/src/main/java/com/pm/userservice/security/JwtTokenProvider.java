package com.pm.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Provider
 * Handles JWT token generation, validation, and claims extraction
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret-key:MyVeryLongSecretKeyForJWTSigningThatIsAtLeast256BitsLongForHS256Algorithm12345}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days in milliseconds
    private long refreshTokenExpirationMs;

    /**
     * Generate JWT token for a user
     */
    public String generateToken(UserDetails userDetails, String enterpriseId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("enterpriseId", enterpriseId);
        claims.put("role", userDetails.getAuthorities().stream()
                .map(Object::toString)
                .findFirst()
                .orElse(""));
        return createToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username, String enterpriseId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("enterpriseId", enterpriseId);
        claims.put("type", "refresh");
        return createToken(claims, username, refreshTokenExpirationMs);
    }

    /**
     * Create a JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get all claims from token
     */
    private Claims getAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get specific claim from token
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract email/username from token
     */
    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extract enterprise ID from token
     */
    public String extractEnterpriseId(String token) {
        return getClaim(token, claims -> (String) claims.get("enterpriseId"));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token with user details
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Validate token with username
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }

    /**
     * Check if token is valid (format and not expired)
     */
    public Boolean isTokenValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid token", e);
            return false;
        }
    }
}

