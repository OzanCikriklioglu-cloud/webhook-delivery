package org.example.webhookdelivery.service.auth;

import org.example.webhookdelivery.domain.User;
import org.example.webhookdelivery.dto.request.LoginRequest;
import org.example.webhookdelivery.dto.request.RegisterRequest;
import org.example.webhookdelivery.dto.response.AuthResponse;
import org.example.webhookdelivery.exception.CustomException;
import org.example.webhookdelivery.repository.UserRepository;
import org.example.webhookdelivery.security.JwtUtil;
import org.springframework.http.HttpStatus;
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

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already in use", HttpStatus.CONFLICT);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        userRepository.save(new User(request.getEmail(), hashedPassword));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}