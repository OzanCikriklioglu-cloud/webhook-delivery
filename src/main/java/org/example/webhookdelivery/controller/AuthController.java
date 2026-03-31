package org.example.webhookdelivery.controller;


import org.example.webhookdelivery.dto.request.LoginRequest;
import org.example.webhookdelivery.dto.request.RegisterRequest;
import org.example.webhookdelivery.dto.response.AuthResponse;
import org.example.webhookdelivery.service.auth.AuthService;
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
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }

    // 🔥 LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}