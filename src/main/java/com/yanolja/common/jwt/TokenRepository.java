package com.yanolja.common.jwt;

import com.yanolja.common.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRepository {
    private static final String KEY_PREFIX = "refreshToken:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtConfig jwtConfig;

    public void saveRefreshToken(String username, String refreshToken) {
        String key = KEY_PREFIX + username;
        // 리프레시 토큰 만료 시간과 동일하게 Redis의 만료 시간 설정
        long expirationInSeconds = jwtConfig.getRefreshTokenExpiration() / 1000;
        redisTemplate.opsForValue().set(key, refreshToken, expirationInSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(String username) {
        String key = KEY_PREFIX + username;
        return redisTemplate.opsForValue().get(key);
    }

    public void removeRefreshToken(String username) {
        String key = KEY_PREFIX + username;
        redisTemplate.delete(key);
    }

    public boolean existsRefreshToken(String username) {
        String key = KEY_PREFIX + username;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
} 