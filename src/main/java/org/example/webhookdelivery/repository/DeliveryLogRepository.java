package org.example.webhookdelivery.repository;

import org.example.webhookdelivery.domain.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {

    List<DeliveryLog> findByEventIdOrderByAttemptNumberDesc(Long eventId);

    List<DeliveryLog> findByEventId(Long eventId);

    long countByEventId(Long eventId);

    List<DeliveryLog> findByStatus(String status);
}
