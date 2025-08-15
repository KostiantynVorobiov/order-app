package com.orderapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderRequest {

    @NotBlank(message = "Idempotency ID must not be empty")
    private String idempotencyId;

    @NotNull(message = "Supplier ID must not be empty")
    private Long supplierId;

    @NotNull(message = "Consumer ID must not be empty")
    private Long consumerId;

    @NotBlank(message = "Title must not be empty")
    private String title;

    @NotNull(message = "Price must not be empty")
    private BigDecimal price;

    public String getIdempotencyId() {
        return idempotencyId;
    }

    public void setIdempotencyId(String idempotencyId) {
        this.idempotencyId = idempotencyId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
