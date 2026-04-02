package org.example.webhookdelivery.service.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    private RetryPolicy retryPolicy;

    @BeforeEach
    void setUp() {
        retryPolicy = new RetryPolicy();
        // Inject @Value fields manually (no Spring context needed)
        ReflectionTestUtils.setField(retryPolicy, "retryDelays", List.of(60_000L, 300_000L, 1_800_000L));
        ReflectionTestUtils.setField(retryPolicy, "maxRetries", 3);
    }

    // ── shouldRetry ────────────────────────────────────────────────────────────

    @Test
    void shouldRetry_whenRetryCountIsZero() {
        assertTrue(retryPolicy.shouldRetry(0));
    }

    @Test
    void shouldRetry_whenRetryCountIsBelowMax() {
        assertTrue(retryPolicy.shouldRetry(1));
        assertTrue(retryPolicy.shouldRetry(2));
    }

    @Test
    void shouldNotRetry_whenRetryCountEqualsMax() {
        assertFalse(retryPolicy.shouldRetry(3));
    }

    @Test
    void shouldNotRetry_whenRetryCountExceedsMax() {
        assertFalse(retryPolicy.shouldRetry(5));
    }

    // ── getDelayMillis ─────────────────────────────────────────────────────────

    @Test
    void getDelayMillis_firstRetry_returns1Minute() {
        assertEquals(60_000L, retryPolicy.getDelayMillis(1));
    }

    @Test
    void getDelayMillis_secondRetry_returns5Minutes() {
        assertEquals(300_000L, retryPolicy.getDelayMillis(2));
    }

    @Test
    void getDelayMillis_thirdRetry_returns30Minutes() {
        assertEquals(1_800_000L, retryPolicy.getDelayMillis(3));
    }

    @Test
    void getDelayMillis_beyondMaxIndex_returnsLastDelay() {
        // Any retry count beyond the list size should clamp to last entry (30m)
        assertEquals(1_800_000L, retryPolicy.getDelayMillis(99));
    }

    @Test
    void getDelayMillis_zeroOrNegative_returnsFirstDelay() {
        assertEquals(60_000L, retryPolicy.getDelayMillis(0));
        assertEquals(60_000L, retryPolicy.getDelayMillis(-1));
    }

    // ── getDelayMinutes ────────────────────────────────────────────────────────

    @Test
    void getDelayMinutes_firstRetry_returns1() {
        assertEquals(1, retryPolicy.getDelayMinutes(1));
    }

    @Test
    void getDelayMinutes_secondRetry_returns5() {
        assertEquals(5, retryPolicy.getDelayMinutes(2));
    }

    @Test
    void getDelayMinutes_thirdRetry_returns30() {
        assertEquals(30, retryPolicy.getDelayMinutes(3));
    }

    // ── getMaxRetries ──────────────────────────────────────────────────────────

    @Test
    void getMaxRetries_returnsConfiguredValue() {
        assertEquals(3, retryPolicy.getMaxRetries());
    }
}
