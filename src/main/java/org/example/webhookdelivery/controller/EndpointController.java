package org.example.webhookdelivery.controller;

import org.example.webhookdelivery.domain.User;
import org.example.webhookdelivery.dto.request.CreateEndpointRequest;
import org.example.webhookdelivery.dto.request.UpdateEndpointRequest;
import org.example.webhookdelivery.dto.response.EndpointResponse;
import org.example.webhookdelivery.repository.UserRepository;
import org.example.webhookdelivery.service.endpoint.EndpointService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing webhook endpoints.
 */
@RestController
@RequestMapping("/api/endpoints")
public class EndpointController {

    private final EndpointService endpointService;
    private final UserRepository userRepository;

    public EndpointController(EndpointService endpointService, UserRepository userRepository) {
        this.endpointService = endpointService;
        this.userRepository = userRepository;
    }

    /**
     * Create a new webhook endpoint.
     */
    @PostMapping
    public ResponseEntity<EndpointResponse> createEndpoint(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateEndpointRequest request) {

        Long userId = getUserId(userDetails);
        EndpointResponse response = endpointService.createEndpoint(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all endpoints for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<EndpointResponse>> getUserEndpoints(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean active) {

        Long userId = getUserId(userDetails);
        List<EndpointResponse> endpoints;

        if (active != null && active) {
            endpoints = endpointService.getActiveEndpoints(userId);
        } else {
            endpoints = endpointService.getUserEndpoints(userId);
        }

        return ResponseEntity.ok(endpoints);
    }

    /**
     * Get a specific endpoint by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EndpointResponse> getEndpoint(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserId(userDetails);
        EndpointResponse response = endpointService.getEndpoint(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing endpoint.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EndpointResponse> updateEndpoint(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEndpointRequest request) {

        Long userId = getUserId(userDetails);
        EndpointResponse response = endpointService.updateEndpoint(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an endpoint.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserId(userDetails);
        endpointService.deleteEndpoint(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Regenerate the secret key for an endpoint.
     */
    @PostMapping("/{id}/regenerate-secret")
    public ResponseEntity<EndpointResponse> regenerateSecretKey(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserId(userDetails);
        EndpointResponse response = endpointService.regenerateSecretKey(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user ID from UserDetails.
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
