package org.example.webhookdelivery.repository;

import org.example.webhookdelivery.domain.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {

    List<WebhookEndpoint> findByUserId(Long userId);

    List<WebhookEndpoint> findByUserIdAndIsActive(Long userId, boolean isActive);

    Optional<WebhookEndpoint> findByIdAndUserId(Long id, Long userId);

    boolean existsByUrlAndUserId(String url, Long userId);

    List<WebhookEndpoint> findByIsActiveTrue();
}

