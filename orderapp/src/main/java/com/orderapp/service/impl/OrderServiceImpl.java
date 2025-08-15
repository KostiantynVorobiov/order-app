package com.orderapp.service.impl;

import com.orderapp.model.Client;
import com.orderapp.model.Order;
import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.OrderRequest;
import com.orderapp.model.dto.OrderResponse;
import com.orderapp.repository.ClientRepository;
import com.orderapp.repository.OrderRepository;
import com.orderapp.service.IdempotencyService;
import com.orderapp.service.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.orderapp.utils.Constants.*;

@Service
public class OrderServiceImpl implements OrderService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${total.customer.benefit}")
    private String totalBenefit;

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final EntityManager entityManager;
    private final IdempotencyService idempotencyService;

    public OrderServiceImpl(ClientRepository clientRepository,
                            OrderRepository orderRepository,
                            EntityManager entityManager,
                            IdempotencyService idempotencyService) {
        this.clientRepository = clientRepository;
        this.orderRepository = orderRepository;
        this.entityManager = entityManager;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    @Override
    public AppResponse<Long> createOrder(OrderRequest orderRequest) {
        logger.info("Create order from seller with id: {} to buyer with id: {}", orderRequest.getSupplierId(), orderRequest.getConsumerId());

        boolean checkAndSetIdempotencyId = idempotencyService.checkAndSetIdempotencyId(orderRequest);
        if (!checkAndSetIdempotencyId) {
            logger.error("An order for customerId: {} is already being processed.", orderRequest.getConsumerId());
            return AppResponse.fail("This order is already being processed.");
        }

        if (orderRequest.getPrice().compareTo(BigDecimal.ZERO) <= ZERO) {
            logger.error("Price must be greater than zero for customerId: {}", orderRequest.getConsumerId());
            return AppResponse.fail("Price must be greater than zero");
        }

        boolean exists = orderRepository.existsByTitleAndSupplierIdAndConsumerId(orderRequest.getTitle(),
                orderRequest.getSupplierId(), orderRequest.getConsumerId());
        if (exists) {
            String businessKey = orderRequest.getTitle() + orderRequest.getSupplierId() + orderRequest.getConsumerId() + EMPTY;
            logger.error("Order already exists with this business key: {}", businessKey);
            return AppResponse.fail("Order already exists with this business key");
        }

        Optional<Client> supplierOptional = clientRepository.findJustClientById(orderRequest.getSupplierId());
        if (supplierOptional.isEmpty()) {
            logger.warn("Supplier with Id : {} not found.", orderRequest.getSupplierId());
            return AppResponse.fail("Supplier not found in DB.");
        }
        Client supplier = supplierOptional.get();

        Optional<Client> consumerOptional = clientRepository.findJustClientById(orderRequest.getConsumerId());
        if (consumerOptional.isEmpty()) {
            logger.warn("Consumer with Id : {} not found.", orderRequest.getConsumerId());
            return AppResponse.fail("Consumer not found in DB.");
        }
        Client consumer = consumerOptional.get();

        if (consumer.getActive() == null || !consumer.getActive()) {
            logger.warn("Consumer with Id : {} is inactive.", consumer.getId());
            return AppResponse.fail("Consumer is inactive.");
        }

        BigDecimal projectedBenefit = consumer.getProfit().subtract(orderRequest.getPrice());

        if (projectedBenefit.compareTo(new BigDecimal(totalBenefit)) < ZERO) {
            logger.warn("Create order for customerId is prohibited: client profit will be less than {}", totalBenefit);
            return AppResponse.fail("Profit limit exceeded.");
        }

        LocalDateTime startProcessing = LocalDateTime.now();

        try {
            int delay = ThreadLocalRandom.current().nextInt(1, 11);
            logger.info("Generated delay: {} seconds", delay);
            Thread.sleep(delay * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted during sleep with message: {}", e.getMessage());
            return AppResponse.fail("Thread was interrupted during processing. Delay interrupted.");
        }

        entityManager.refresh(consumer);

        if (consumer.getActive() == null || !consumer.getActive()) {
            logger.warn("Consumer with Id : {} became inactive during processing.", consumer.getId());
            return AppResponse.fail("Consumer became inactive during processing.");
        }

        Order order = new Order();
        order.setTitle(orderRequest.getTitle());
        order.setSupplier(supplier);
        order.setConsumer(consumer);
        order.setPrice(orderRequest.getPrice());
        order.setProcessingStartAt(startProcessing);
        order.setProcessingEndAt(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());

        supplier.setProfit(supplier.getProfit().add(order.getPrice()));
        clientRepository.save(supplier);

        consumer.setProfit(consumer.getProfit().subtract(order.getPrice()));
        clientRepository.save(consumer);

        Order saved = orderRepository.save(order);
        return AppResponse.ok(saved.getId(), SUCCESS);
    }

    @Override
    public AppResponse<OrderResponse> getOrderById(Long orderId) {
        logger.info("Get order by ID: {}", orderId);
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            logger.warn("Order with Id : {} not found.", orderId);
            return AppResponse.fail("Order not found in DB.");
        }
        OrderResponse orderResponse = new OrderResponse(optionalOrder.get());
        return AppResponse.ok(orderResponse, SUCCESS);
    }

    @Override
    public AppResponse<List<OrderResponse>> getAllOrders() {
        logger.info("Get all orders from DB");
        List<OrderResponse> orderResponses = orderRepository.findAll().stream()
                .map(OrderResponse::new)
                .toList();
        return AppResponse.ok(orderResponses, SUCCESS);
    }
}
