package com.lomafood.auth.service;

import com.lomafood.auth.dto.*;
import com.lomafood.auth.entity.*;
import com.lomafood.auth.repository.UserRepository;
import com.lomafood.auth.security.JwtService;
import com.lomafood.shared.dto.ApiResponse;
import com.lomafood.shared.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwt;

    public ApiResponse<?> register(RegisterRequest req) {
        if (repo.findByEmail(req.getEmail()).isPresent()) {
            throw new AppException(400, "Email already exists");
        }
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setName(req.getName());
        u.setRole(Role.USER);
        u.setEnabled(true);
        repo.save(u);
        return ApiResponse.success("Registered successfully");
    }

    public ApiResponse<?> login(LoginRequest req) {
        User u = repo.findByEmail(req.getEmail())
            .orElseThrow(() -> new AppException(404, "User not found"));
        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new AppException(401, "Wrong password");
        }
        String token = jwt.generateToken(u.getEmail(), u.getRole().name());
        return ApiResponse.success("Login successful", token);
    }
}
