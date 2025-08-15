package com.orderapp.model.dto;

import com.orderapp.model.Client;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ClientResponse {

    @Schema(implementation = Long.class, description = "Client name", example = "Bob Alison")
    private String name;

    @Schema(implementation = Long.class, description = "Client email", example = "bob-alison.email@gmail.com")
    private String email;

    @Schema(implementation = Long.class, description = "Client phone number", example = "+380966473403")
    private String phoneNumber;

    @Schema(implementation = Long.class, description = "Client status", example = "true")
    private Boolean active;

    @Schema(implementation = Long.class, description = "The time when the client was deactivated", example = "2025-08-07T12:30:00")
    private LocalDateTime inactiveAt;

    @Schema(implementation = Long.class, description = "The time when the client was created", example = "2025-08-05T13:00:00")
    private LocalDateTime createdAt;

    @Schema(implementation = Long.class, description = "All orders where client supplier", example = "29")
    private List<OrderResponse> suppliedOrders;

    @Schema(implementation = Long.class, description = "All orders where client consumer", example = "101")
    private List<OrderResponse> consumedOrders;

    @Schema(implementation = Long.class, description = "Total client profit", example = "325.75")
    private BigDecimal totalProfit;

    public ClientResponse() {
    }

    public ClientResponse(Client client) {
        this.name = client.getName();
        this.email = client.getEmail();
        this.phoneNumber = client.getPhoneNumber();
        this.active = client.getActive();
        this.inactiveAt = client.getInactiveAt();
        this.createdAt = client.getCreatedAt();
        this.totalProfit = client.getProfit();
        this.suppliedOrders = client.getSuppliedOrders().stream()
                .map(OrderResponse::new)
                .toList();
        this.consumedOrders = client.getConsumedOrders().stream()
                .map(OrderResponse::new)
                .toList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getInactiveAt() {
        return inactiveAt;
    }

    public void setInactiveAt(LocalDateTime inactiveAt) {
        this.inactiveAt = inactiveAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderResponse> getSuppliedOrders() {
        return suppliedOrders;
    }

    public void setSuppliedOrders(List<OrderResponse> suppliedOrders) {
        this.suppliedOrders = suppliedOrders;
    }

    public List<OrderResponse> getConsumedOrders() {
        return consumedOrders;
    }

    public void setConsumedOrders(List<OrderResponse> consumedOrders) {
        this.consumedOrders = consumedOrders;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }
}
