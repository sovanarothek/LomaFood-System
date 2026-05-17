package com.lomafood.auth.service;

import com.lomafood.auth.dto.*;
import com.lomafood.auth.entity.*;
import com.lomafood.auth.repository.RefreshTokenRepository;
import com.lomafood.auth.repository.UserRepository;
import com.lomafood.auth.security.JwtService;
import com.lomafood.shared.dto.ApiResponse;
import com.lomafood.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final EmailService emailService;

    public ApiResponse<?> register(RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new AppException(400, "Email already exists");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .name(req.getName())
                .role(Role.GUEST)
                .enabled(false)
                .emailVerified(false)
                .build();
        userRepo.save(user);
        String otp = generateOtp();
        redisService.saveOtp(req.getEmail(), otp, 5);
        emailService.sendOtp(req.getEmail(), otp);
        return ApiResponse.success("Registered! Check your email for OTP.");
    }

    public ApiResponse<?> verifyOtp(OtpRequest req) {
        String savedOtp = redisService.getOtp(req.getEmail());
        if (savedOtp == null) throw new AppException(400, "OTP expired or not found");
        if (!savedOtp.equals(req.getOtp())) throw new AppException(400, "Invalid OTP");
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(404, "User not found"));
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRole(Role.USER);
        userRepo.save(user);
        redisService.deleteOtp(req.getEmail());
        return ApiResponse.success("Email verified! You can now login.");
    }

    public ApiResponse<?> resendOtp(String email) {
        userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
        String otp = generateOtp();
        redisService.saveOtp(email, otp, 5);
        emailService.sendOtp(email, otp);
        return ApiResponse.success("OTP resent to " + email);
    }

    public ApiResponse<?> login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new AppException(404, "User not found"));
        if (!user.isEnabled()) throw new AppException(403, "Account not verified.");
        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new AppException(401, "Wrong password");
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenRepo.revokeAllUserTokens(user);
        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepo.save(rt);
        return ApiResponse.success("Login successful", new TokenResponse(accessToken, refreshToken));
    }

    public ApiResponse<?> refreshToken(String refreshToken) {
        RefreshToken rt = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(401, "Invalid refresh token"));
        if (rt.isRevoked()) throw new AppException(401, "Refresh token revoked");
        if (rt.isExpired()) throw new AppException(401, "Refresh token expired");
        User user = rt.getUser();
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());
        rt.setRevoked(true);
        refreshTokenRepo.save(rt);
        RefreshToken newRt = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepo.save(newRt);
        return ApiResponse.success("Token refreshed", new TokenResponse(newAccessToken, newRefreshToken));
    }

    public ApiResponse<?> logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new AppException(400, "Invalid token");
        String token = authHeader.substring(7);
        Date expiry = jwtService.extractExpiration(token);
        long ttl = expiry.getTime() - System.currentTimeMillis();
        redisService.blacklistToken(token, ttl);
        String email = jwtService.extractEmail(token);
        userRepo.findByEmail(email).ifPresent(refreshTokenRepo::revokeAllUserTokens);
        return ApiResponse.success("Logged out successfully");
    }

    public ApiResponse<?> forgotPassword(String email) {
        userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
        String resetToken = UUID.randomUUID().toString();
        redisService.saveResetToken(email, resetToken, 15);
        String resetLink = "http://localhost:8081/auth/reset-password?token=" + resetToken;
        emailService.sendPasswordResetLink(email, resetLink);
        return ApiResponse.success("Password reset link sent to " + email);
    }

    public ApiResponse<?> resetPassword(ResetPasswordRequest req) {
        String email = redisService.getEmailByResetToken(req.getToken());
        if (email == null) throw new AppException(400, "Reset token expired or invalid");
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "User not found"));
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
        redisService.deleteResetToken(req.getToken());
        refreshTokenRepo.revokeAllUserTokens(user);
        return ApiResponse.success("Password reset successfully. Please login again.");
    }

    public ApiResponse<?> googleLogin(GoogleAuthRequest req) {
        User user = userRepo.findByEmail(req.getEmail()).orElseGet(() -> {
            User newUser = User.builder()
                    .email(req.getEmail())
                    .name(req.getName())
                    .provider("google")
                    .providerId(req.getProviderId())
                    .role(Role.USER)
                    .enabled(true)
                    .emailVerified(true)
                    .build();
            return userRepo.save(newUser);
        });
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenRepo.revokeAllUserTokens(user);
        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepo.save(rt);
        return ApiResponse.success("Google login successful", new TokenResponse(accessToken, refreshToken));
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
