package com.orderapp.model.dto;

import com.orderapp.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {

    @Schema(implementation = String.class, description = "Order title", example = "Food")
    private String title;

    @Schema(implementation = Long.class, description = "Supplier ID", example = "42")
    private Long supplierId;

    @Schema(implementation = Long.class, description = "Consumer ID", example = "17")
    private Long consumerId;

    @Schema(implementation = BigDecimal.class, description = "Order price", example = "775.45")
    private BigDecimal price;

    @Schema(implementation = LocalDateTime.class, description = "The time when the order was created", example = "2025-08-01T12:30:00")
    private LocalDateTime createdAt;

    public OrderResponse() {
    }

    public OrderResponse(Order order) {
        this.title = order.getTitle();
        this.supplierId = order.getSupplier().getId();
        this.consumerId = order.getConsumer().getId();
        this.price = order.getPrice();
        this.createdAt = order.getCreatedAt();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
