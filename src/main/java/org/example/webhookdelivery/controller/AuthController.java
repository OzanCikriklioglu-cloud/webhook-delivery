package org.example.webhookdelivery.controller;


import jakarta.validation.Valid;
import org.example.webhookdelivery.dto.request.LoginRequest;
import org.example.webhookdelivery.dto.request.RegisterRequest;
import org.example.webhookdelivery.dto.response.AuthResponse;
import org.example.webhookdelivery.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 🔥 REGISTER
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    // 🔥 LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}