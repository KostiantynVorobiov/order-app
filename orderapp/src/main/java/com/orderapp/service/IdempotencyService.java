package com.orderapp.service;

import com.orderapp.model.dto.OrderRequest;

public interface IdempotencyService {

    boolean checkAndSetIdempotencyId(OrderRequest orderRequest);

    void clearIdempotencyLock(String idempotencyId);
}
