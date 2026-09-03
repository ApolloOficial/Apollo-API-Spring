package org.apollo.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apollo.api.security.AuthenticatedUser;
import org.apollo.api.security.JwtAuthenticationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final int MINIMUM_HMAC_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${apollo.jwt.secret}") String secret,
            @Value("${apollo.jwt.expiration-ms}") long expirationMs,
            @Value("${apollo.jwt.issuer}") String issuer,
            @Value("${apollo.jwt.audience}") String audience
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        if (keyBytes.length < MINIMUM_HMAC_KEY_BYTES) {
            throw new IllegalStateException("JWT secret must contain at least 256 bits");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(AuthenticatedUser user) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("userId", user.getUserId())
                .claim("companyId", user.getCompanyId())
                .claim("userType", user.getUserType())
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public JwtAuthenticationData extractAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = getRequiredLong(claims, "userId");
        Long companyId = getRequiredLong(claims, "companyId");
        String userType = claims.get("userType", String.class);
        String email = claims.getSubject();
        if (userType == null || userType.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("JWT sem identidade obrigatória");
        }
        return new JwtAuthenticationData(userId, companyId, userType, email);
    }

    private Long getRequiredLong(Claims claims, String claim) {
        Number value = claims.get(claim, Number.class);
        if (value == null) {
            throw new IllegalArgumentException("JWT sem claim obrigatória: " + claim);
        }
        return value.longValue();
    }
}
