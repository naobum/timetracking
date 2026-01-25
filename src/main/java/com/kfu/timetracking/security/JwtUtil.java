package com.kfu.timetracking.security;

import com.kfu.timetracking.models.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-expiration:900000}") // 15 минут
    private Long accessExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 7 дней
    private Long refreshExpiration;
    
    public static final String ACCESS_COOKIE_NAME = "access-token";
    public static final String REFRESH_COOKIE_NAME = "refresh-token";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String username, String role) {
        return generateToken(username, role, TokenType.ACCESS, accessExpiration);
    }
    
    public String generateRefreshToken(String username, String role) {
        return generateToken(username, role, TokenType.REFRESH, refreshExpiration);
    }

    private String generateToken(String username, String role, TokenType type, Long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", type.name());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    public TokenType extractTokenType(String token) {
        String type = extractClaim(token, claims -> claims.get("type", String.class));
        return TokenType.valueOf(type);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Cookie generateAccessCookie(String username, String role) {
        String token = generateAccessToken(username, role);
        Cookie cookie = new Cookie(ACCESS_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessExpiration / 1000));
        return cookie;
    }
    
    public Cookie generateRefreshCookie(String username, String role) {
        String token = generateRefreshToken(username, role);
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshExpiration / 1000));
        return cookie;
    }

    public Cookie getDeleteCookie(String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
