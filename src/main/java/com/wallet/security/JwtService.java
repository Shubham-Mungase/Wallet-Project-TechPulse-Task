package com.wallet.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wallet.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationTime;

	public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationTime) {

		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

		this.expirationTime = expirationTime;
	}

	public String generateToken(User user) {

		Date now = new Date();

		return Jwts.builder().subject(user.getEmail()).claim("userId", user.getId())
				.claim("role", user.getRole().name()).issuedAt(now).expiration(new Date(now.getTime() + expirationTime))
				.signWith(secretKey).compact();
	}

	public String extractEmail(String token) {

		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, User user) {

		String email = extractEmail(token);

		return email.equals(user.getEmail()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {

		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}
}