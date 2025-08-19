package com.epam.gym_crm.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.epam.gym_crm.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final JwtConfig jwtConfig;

	public JwtTokenProvider(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
	}

	public String generateJwtToken(JwtUserDetails userDetails) {
		Date expireDate = new Date(new Date().getTime() + jwtConfig.getExpirationMs());
		return Jwts.builder().setSubject(Long.toString(userDetails.getId()))
				.claim("username", userDetails.getUsername()).setIssuedAt(new Date()).setExpiration(expireDate)
				.signWith(getKey(), SignatureAlgorithm.HS256).compact();
	}

	public Claims getClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
	}

	public <T> T exportToken(String token, Function<Claims, T> claimsFunc) {
		Claims claims = getClaims(token);
		return claimsFunc.apply(claims);
	}

	public Long getUserIdFromToken(String token) {
		return exportToken(token, claims -> Long.parseLong(claims.getSubject()));
	}

	public String getUsernameFromToken(String token) {
		return exportToken(token, claims -> claims.get("username", String.class));
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = getClaims(token);
			return !isTokenExpired(claims);
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private boolean isTokenExpired(Claims claims) {
		Date expiration = claims.getExpiration();
		return expiration.before(new Date());
	}

	public Key getKey() {
		return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
	}

}
