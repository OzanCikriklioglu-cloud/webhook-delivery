package org.example.webhookdelivery.service.auth;

import org.example.webhookdelivery.domain.User;
import org.example.webhookdelivery.dto.request.LoginRequest;
import org.example.webhookdelivery.dto.request.RegisterRequest;
import org.example.webhookdelivery.dto.response.AuthResponse;
import org.example.webhookdelivery.repository.UserRepository;
import org.example.webhookdelivery.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 🔥 REGISTER
    public void register(RegisterRequest request) {

        // email already exists mi?
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // password hash
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // user oluştur
        User user = new User(
                request.getEmail(),
                hashedPassword
        );

        // save
        userRepository.save(user);
    }

    // 🔥 LOGIN
    public AuthResponse login(LoginRequest request) {

        // user bul
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // JWT üret
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}