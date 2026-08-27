package com.nightfriday.onlineshopping.services;

import com.nightfriday.onlineshopping.persistence.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.token-validity-in-milliseconds}")
  private long expiration;

  public String encode(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("id", user.getId());
    claims.put("name", user.getName());
    claims.put("email", user.getEmail());
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .claims(claims)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  public User decode(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    User user = new User();
    user.setId(claims.get("id", String.class));
    user.setName(claims.get("name", String.class));
    user.setEmail(claims.get("email", String.class));
    return user;
  }

  private SecretKey getSigningKey() {
    byte[] encodedBytes = Decoders.BASE64.decode(secretKey);

    return Keys.hmacShaKeyFor(encodedBytes);
  }
}
