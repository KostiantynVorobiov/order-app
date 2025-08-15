package com.orderapp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"title", "supplier_id", "consumer_id"}
        )
)
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Client supplier;

    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    private Client consumer;

    private BigDecimal price;

    private LocalDateTime processingStartAt;

    private LocalDateTime processingEndAt;

    private LocalDateTime createdAt;

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Client getSupplier() {
        return supplier;
    }

    public void setSupplier(Client supplier) {
        this.supplier = supplier;
    }

    public Client getConsumer() {
        return consumer;
    }

    public void setConsumer(Client consumer) {
        this.consumer = consumer;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getProcessingStartAt() {
        return processingStartAt;
    }

    public void setProcessingStartAt(LocalDateTime processingStartAt) {
        this.processingStartAt = processingStartAt;
    }

    public LocalDateTime getProcessingEndAt() {
        return processingEndAt;
    }

    public void setProcessingEndAt(LocalDateTime processingEndAt) {
        this.processingEndAt = processingEndAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
