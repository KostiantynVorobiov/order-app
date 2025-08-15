package com.orderapp.service;

import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.OrderRequest;
import com.orderapp.model.dto.OrderResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {

    @Transactional
    AppResponse<Long> createOrder(OrderRequest orderRequest);

    AppResponse<OrderResponse> getOrderById(Long orderId);

    AppResponse<List<OrderResponse>> getAllOrders();
}
