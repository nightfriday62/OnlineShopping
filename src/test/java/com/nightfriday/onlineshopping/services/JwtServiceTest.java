package com.nightfriday.onlineshopping.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nightfriday.onlineshopping.persistence.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {
  private static final String TEST_SECRET =
      "ZnJlbmNoLWlzLWhhcmQtdG8tbGVhcm4tY2VzdC10cm9wLWRpZmZpY2lsZQ==";
  private static final long TOKEN_VALIDITY_IN_MILLISECONDS = 3_600_000L;
  private static final long JWT_DATE_PRECISION_TOLERANCE_IN_MILLISECONDS = 1_000L;

  private final JwtService jwtService = new JwtService();
  private SecretKey signingKey;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
    ReflectionTestUtils.setField(
        jwtService, "expiration", TOKEN_VALIDITY_IN_MILLISECONDS);
    signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
  }

  @Test
  void encodeIncludesClaimsAndStandardTimestamps() {
    User user = createUser();
    Date beforeEncoding = new Date();

    String token = jwtService.encode(user);

    Date afterEncoding = new Date();
    Claims claims = parseClaims(token);

    assertEquals(user.getId(), claims.get("id", String.class));
    assertEquals(user.getName(), claims.get("name", String.class));
    assertEquals(user.getEmail(), claims.get("email", String.class));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertTrue(
        claims.getIssuedAt().getTime()
            >= beforeEncoding.getTime() - JWT_DATE_PRECISION_TOLERANCE_IN_MILLISECONDS);
    assertTrue(
        claims.getIssuedAt().getTime()
            <= afterEncoding.getTime() + JWT_DATE_PRECISION_TOLERANCE_IN_MILLISECONDS);
    assertTrue(
        Math.abs(
                (claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                    - TOKEN_VALIDITY_IN_MILLISECONDS)
            <= JWT_DATE_PRECISION_TOLERANCE_IN_MILLISECONDS);
  }

  @Test
  void decodeReturnsUserFromTokenClaims() {
    User user = createUser();
    String token = jwtService.encode(user);

    User decodedUser = jwtService.decode(token);

    assertEquals(user.getId(), decodedUser.getId());
    assertEquals(user.getName(), decodedUser.getName());
    assertEquals(user.getEmail(), decodedUser.getEmail());
  }

  @Test
  void decodeThrowsWhenTokenHasExpired() {
    User user = createUser();
    String token = createExpiredToken(user);

    assertThrows(ExpiredJwtException.class, () -> jwtService.decode(token));
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private String createExpiredToken(User user) {
    Date issuedAt = new Date(System.currentTimeMillis() - 10_000L);
    Date expiration = new Date(System.currentTimeMillis() - 1_000L);

    return Jwts.builder()
        .claim("id", user.getId())
        .claim("name", user.getName())
        .claim("email", user.getEmail())
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(signingKey)
        .compact();
  }

  private User createUser() {
    User user = new User();
    user.setId("1");
    user.setName("Ada");
    user.setEmail("ada@example.com");
    user.setPassword("ignored");
    return user;
  }
}
