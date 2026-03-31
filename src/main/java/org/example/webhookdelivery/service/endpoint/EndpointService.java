package org.example.webhookdelivery.service.endpoint;

import org.example.webhookdelivery.domain.User;
import org.example.webhookdelivery.domain.WebhookEndpoint;
import org.example.webhookdelivery.dto.request.CreateEndpointRequest;
import org.example.webhookdelivery.dto.request.UpdateEndpointRequest;
import org.example.webhookdelivery.dto.response.EndpointResponse;
import org.example.webhookdelivery.exception.CustomException;
import org.example.webhookdelivery.repository.UserRepository;
import org.example.webhookdelivery.repository.WebhookEndpointRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing webhook endpoints.
 */
@Service
@Transactional
public class EndpointService {

    private final WebhookEndpointRepository endpointRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public EndpointService(WebhookEndpointRepository endpointRepository,
                           UserRepository userRepository) {
        this.endpointRepository = endpointRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new webhook endpoint for a user.
     */
    public EndpointResponse createEndpoint(Long userId, CreateEndpointRequest request) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // Check if URL already exists for this user
        if (endpointRepository.existsByUrlAndUserId(request.getUrl(), userId)) {
            throw new CustomException("Endpoint with this URL already exists", HttpStatus.CONFLICT);
        }

        // Generate a secret key for webhook signature verification
        String secretKey = generateSecretKey();

        // Create endpoint
        WebhookEndpoint endpoint = new WebhookEndpoint(
                request.getName(),
                request.getUrl(),
                userId,
                secretKey
        );

        WebhookEndpoint savedEndpoint = endpointRepository.save(endpoint);
        return new EndpointResponse(savedEndpoint);
    }

    /**
     * Get all endpoints for a user.
     */
    @Transactional(readOnly = true)
    public List<EndpointResponse> getUserEndpoints(Long userId) {
        return endpointRepository.findByUserId(userId).stream()
                .map(EndpointResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get active endpoints for a user.
     */
    @Transactional(readOnly = true)
    public List<EndpointResponse> getActiveEndpoints(Long userId) {
        return endpointRepository.findByUserIdAndIsActive(userId, true).stream()
                .map(EndpointResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific endpoint by ID.
     */
    @Transactional(readOnly = true)
    public EndpointResponse getEndpoint(Long userId, Long endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndUserId(endpointId, userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));
        return new EndpointResponse(endpoint);
    }

    /**
     * Update an existing endpoint.
     */
    public EndpointResponse updateEndpoint(Long userId, Long endpointId, UpdateEndpointRequest request) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndUserId(endpointId, userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null) {
            endpoint.setName(request.getName());
        }

        if (request.getUrl() != null) {
            // Check if new URL conflicts with existing endpoints
            if (!endpoint.getUrl().equals(request.getUrl()) &&
                    endpointRepository.existsByUrlAndUserId(request.getUrl(), userId)) {
                throw new CustomException("Endpoint with this URL already exists", HttpStatus.CONFLICT);
            }
            endpoint.setUrl(request.getUrl());
        }

        if (request.getIsActive() != null) {
            endpoint.setActive(request.getIsActive());
        }

        WebhookEndpoint updatedEndpoint = endpointRepository.save(endpoint);
        return new EndpointResponse(updatedEndpoint);
    }

    /**
     * Delete an endpoint.
     */
    public void deleteEndpoint(Long userId, Long endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndUserId(endpointId, userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        endpointRepository.delete(endpoint);
    }

    /**
     * Regenerate the secret key for an endpoint.
     */
    public EndpointResponse regenerateSecretKey(Long userId, Long endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findByIdAndUserId(endpointId, userId)
                .orElseThrow(() -> new CustomException("Endpoint not found", HttpStatus.NOT_FOUND));

        endpoint.setSecretKey(generateSecretKey());
        WebhookEndpoint updatedEndpoint = endpointRepository.save(endpoint);
        return new EndpointResponse(updatedEndpoint);
    }

    /**
     * Generate a secure secret key for webhook signature verification.
     */
    private String generateSecretKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
