package com.orderapp.model;

import com.orderapp.model.dto.ClientRequest;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @Column(name = "client_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private Boolean active;

    private LocalDateTime inactiveAt;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "supplier")
    private Set<Order> suppliedOrders = new HashSet<>();

    @OneToMany(mappedBy = "consumer")
    private Set<Order> consumedOrders = new HashSet<>();

    private BigDecimal profit;

    public Client() {
    }

    public Client(ClientRequest clientRequest) {
        this.name = clientRequest.getName();
        this.email = clientRequest.getEmail();
        this.phoneNumber = clientRequest.getPhoneNumber();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.profit = BigDecimal.ZERO;
    }

    public Client(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void addSuppliedOrder(Order order) {
        if (this.suppliedOrders == null) {
            this.suppliedOrders = new HashSet<>();
        }
        order.setSupplier(this);
        this.suppliedOrders.add(order);
    }

    public void addConsumedOrders(Order order) {
        if (this.consumedOrders == null) {
            this.consumedOrders = new HashSet<>();
        }
        order.setConsumer(this);
        this.consumedOrders.add(order);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<Order> getSuppliedOrders() {
        return suppliedOrders;
    }

    public void setSuppliedOrders(Set<Order> suppliedOrders) {
        this.suppliedOrders = suppliedOrders;
    }

    public Set<Order> getConsumedOrders() {
        return consumedOrders;
    }

    public void setConsumedOrders(Set<Order> consumedOrders) {
        this.consumedOrders = consumedOrders;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
