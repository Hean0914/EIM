package com.company.enterprise.utils;

import com.company.enterprise.common.TokenUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expireMillis;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expire-hours}") long expireHours) {
        this.secretKey = Keys.hmacShaKeyFor(sha256(secret));
        this.expireMillis = expireHours * 60 * 60 * 1000;
    }

    public String generateToken(TokenUser user) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("realName", user.getRealName())
                .claim("roleId", user.getRoleId())
                .claim("roleCode", user.getRoleCode())
                .claim("employeeId", user.getEmployeeId())
                .claim("avatar", user.getAvatar())
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public TokenUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new TokenUser(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("realName", String.class),
                toLong(claims.get("roleId")),
                claims.get("roleCode", String.class),
                toLong(claims.get("employeeId")),
                claims.get("avatar", String.class)
        );
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    private byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
