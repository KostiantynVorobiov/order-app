package com.orderapp.service.impl;

import com.orderapp.model.dto.OrderRequest;
import com.orderapp.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.orderapp.utils.Constants.CACHE_FORMAT;
import static com.orderapp.utils.Constants.LOCKED;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    @Value("${idempotency.key.ttl.seconds}")
    private int idempotencyKeyTtl;

    private final StringRedisTemplate redisTemplate;

    public IdempotencyServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean checkAndSetIdempotencyId(OrderRequest orderRequest) {
        String redisKey = String.format(CACHE_FORMAT, orderRequest.getTitle(),
                orderRequest.getSupplierId(), orderRequest.getConsumerId());

        Boolean success = redisTemplate.opsForValue().setIfAbsent(orderRequest.getIdempotencyId(),
                LOCKED, Duration.ofSeconds(idempotencyKeyTtl));
        return success != null && success;
    }

    @Override
    public void clearIdempotencyLock(String idempotencyId) {
        redisTemplate.delete(idempotencyId);
    }
}
