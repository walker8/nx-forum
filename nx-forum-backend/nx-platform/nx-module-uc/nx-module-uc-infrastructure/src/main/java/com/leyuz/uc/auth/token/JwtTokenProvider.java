package com.leyuz.uc.auth.token;

import cn.hutool.core.convert.Convert;
import com.leyuz.uc.auth.AuthUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * JWT token 工具类
 * <p>
 * 用于生成/校验/解析 JWT Token
 *
 * @author Walker
 * @since 2024/1/7
 */
@Component
public class JwtTokenProvider {

    /**
     * 签名密钥，用于签名 Access Token
     */
    @Value("${jwt.secret-key:SecretKey012345678901234567890123456789012345678901234567890123456789}")
    private String secretKey;

    /**
     * Base64 编码后的签名密钥，用于校验/解析 Access Token
     */
    private byte[] secretKeyBytes;


    /**
     * 初始化方法
     * 对签名密钥进行 Base64 编码
     */
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * 创建Token
     * 认证成功后的用户信息会被封装到 Authentication 对象中，然后通过 JwtTokenProvider#createToken(Authentication) 方法创建 Token 字符串
     *
     * @param authentication 用户认证信息
     * @return Token 字符串
     */
    public String createToken(Authentication authentication) {
        AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
        return createToken(userDetails.getUserId(), authentication.getName());
    }

    public String createToken(Long userId, String userName) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + 3600 * 24 * 1000L);
        return Jwts.builder()
                .subject(userName)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expirationTime)
                .signWith(Keys.hmacShaKeyFor(getSecretKeyBytes())).compact();
    }


    /**
     * 根据给定的令牌解析出用户认证信息
     *
     * @param token JWT Token
     * @return 用户认证信息
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getTokenClaims(token);

        AuthUserDetails userDetails = new AuthUserDetails();
        userDetails.setUserId(Convert.toLong(claims.get("userId"))); // 用户ID

        return new UsernamePasswordAuthenticationToken(userDetails, "", null);
    }

    /**
     * 校验Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        Jwts.parser().setSigningKey(getSecretKeyBytes()).build().parseClaimsJws(token);
        return true;
    }

    /**
     * 获取Token中的用户名
     *
     * @param token Token
     * @return
     */
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(getSecretKeyBytes()).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 获取Token的Claims，claims中包含了用户的基本信息
     *
     * @param token
     * @return
     */
    public Claims getTokenClaims(String token) {
        return Jwts.parser().setSigningKey(getSecretKeyBytes()).build().parseClaimsJws(token).getBody();
    }

    /**
     * 获取签名密钥的字节数组
     *
     * @return 签名密钥的字节数组
     */
    public byte[] getSecretKeyBytes() {
        if (secretKeyBytes == null) {
            try {
                secretKeyBytes = Decoders.BASE64.decode(secretKey);
            } catch (DecodingException e) {
                secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            }
        }
        return secretKeyBytes;
    }

    /**
     * 从请求头中获取Token
     *
     * @return Token 字符串
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

}
