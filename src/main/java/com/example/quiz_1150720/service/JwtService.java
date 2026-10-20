
package com.example.quiz_1150720.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	// 從 application.properties 讀取金鑰
	@Value("${jwt.secret}")
	private String secret;

	// 從 application.properties 讀取 Token 有效時間
	@Value("${jwt.expiration}")
	private long expiration;

	// 將 Base64 金鑰轉成 JWT 使用的金鑰
	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// 產生 JWT Token
	public String generateToken(String account) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder().subject(account).issuedAt(now).expiration(expiryDate).signWith(getSigningKey()).compact();
	}

	// 驗證 Token，並取得使用者帳號
	public String extractAccount(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

		return claims.getSubject();
	}

	// 確認 Token 是否有效
	public boolean isTokenValid(String token, String account) {
		try {
			return account.equals(extractAccount(token));
		} catch (Exception e) {
			return false;
		}
	}
}
