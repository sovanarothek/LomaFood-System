package com.lomafood.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String OTP_PREFIX = "otp:";
    private static final String RESET_PREFIX = "reset:";

    // ---- TOKEN BLACKLIST ----
    public void blacklistToken(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "revoked",
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token)
        );
    }

    // ---- OTP ----
    public void saveOtp(String email, String otp, long ttlMinutes) {
        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otp,
                ttlMinutes,
                TimeUnit.MINUTES
        );
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(OTP_PREFIX + email);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
    }

    // ---- PASSWORD RESET TOKEN ----
    public void saveResetToken(String email, String token, long ttlMinutes) {
        redisTemplate.opsForValue().set(
                RESET_PREFIX + token,
                email,
                ttlMinutes,
                TimeUnit.MINUTES
        );
    }

    public String getEmailByResetToken(String token) {
        return redisTemplate.opsForValue().get(RESET_PREFIX + token);
    }

    public void deleteResetToken(String token) {
        redisTemplate.delete(RESET_PREFIX + token);
    }
}
