package dev.pulsestack.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars!!";
    private static final long EXPIRY_MS = 3_600_000L; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRY_MS);
    }

    @Test
    void generateToken_containsUsername() {
        String token = jwtService.generateToken("alice");
        Claims claims = parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("alice");
    }

    @Test
    void generateToken_isNotExpired() {
        String token = jwtService.generateToken("alice");
        Claims claims = parseClaims(token);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void generateToken_expiresApproximatelyAfterConfiguredTime() {
        String token = jwtService.generateToken("alice");
        Claims claims = parseClaims(token);
        long ttlMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(ttlMs).isBetween(EXPIRY_MS - 1000, EXPIRY_MS + 1000);
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String tokenA = jwtService.generateToken("alice");
        String tokenB = jwtService.generateToken("bob");
        assertThat(tokenA).isNotEqualTo(tokenB);
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
