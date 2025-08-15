package com.orderapp.unit;

import com.orderapp.model.dto.OrderRequest;
import com.orderapp.service.impl.IdempotencyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdempotencyServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private IdempotencyServiceImpl idempotencyService;

    private OrderRequest orderRequest;
    private static final String IDEMPOTENCY_ID = "test-idempotency-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(idempotencyService, "idempotencyKeyTtl", 300);

        orderRequest = new OrderRequest();
        orderRequest.setIdempotencyId(IDEMPOTENCY_ID);
        orderRequest.setTitle("Test Order");
        orderRequest.setSupplierId(1L);
        orderRequest.setConsumerId(2L);
    }

    @Test
    void checkAndSetIdempotencyId_keyDoesNotExist_returnsTrue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        boolean result = idempotencyService.checkAndSetIdempotencyId(orderRequest);

        assertTrue(result);
        verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void checkAndSetIdempotencyId_keyAlreadyExists_returnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), eq(Duration.ofSeconds(300)))).thenReturn(false);

        boolean result = idempotencyService.checkAndSetIdempotencyId(orderRequest);

        assertFalse(result);

        verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), eq(Duration.ofSeconds(300)));
    }

    @Test
    void clearIdempotencyLock_deletesKey() {
        idempotencyService.clearIdempotencyLock(IDEMPOTENCY_ID);

        verify(redisTemplate, times(1)).delete(anyString());
    }
}
