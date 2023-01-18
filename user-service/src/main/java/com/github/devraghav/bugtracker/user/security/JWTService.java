package com.github.devraghav.bugtracker.user.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.devraghav.bugtracker.user.dto.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JWTService {
  @Autowired private ObjectMapper objectMapper;

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.secret.expiration}")
  private String expirationTime;

  private Key key;

  @PostConstruct
  void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  Date getExpirationDateFromToken(String token) {
    return getAllClaimsFromToken(token).getExpiration();
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  public String generateToken(User user) {
    var claims = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() {});
    return doGenerateToken(claims, user.id());
  }

  private String doGenerateToken(Map<String, Object> claims, String userId) {
    Long expirationTimeLong = Long.parseLong(expirationTime); // in second
    final Date createdDate = new Date();
    final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userId)
        .setIssuedAt(createdDate)
        .setExpiration(expirationDate)
        .signWith(key)
        .compact();
  }

  Boolean validateToken(String token) {
    boolean isTokenNotExpired = isTokenExpired(token);
    log.atDebug().log("is token expired?  {}", isTokenNotExpired);
    return BooleanUtils.negate(isTokenNotExpired);
  }
}
