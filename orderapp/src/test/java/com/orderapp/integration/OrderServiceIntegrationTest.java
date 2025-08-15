package com.orderapp.integration;

import com.orderapp.model.Client;
import com.orderapp.model.Order;
import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.OrderRequest;
import com.orderapp.model.dto.OrderResponse;
import com.orderapp.repository.ClientRepository;
import com.orderapp.repository.OrderRepository;
import com.orderapp.service.IdempotencyService;
import com.orderapp.service.OrderService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Testcontainers
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private IdempotencyService idempotencyService;
    @Autowired
    private OrderRepository orderRepository;

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0.5-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    private Client supplier;
    private Client consumer;
    private OrderRequest validOrderRequest;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();

        supplier = new Client("Supplier", "supplier@email.com", "111");
        supplier.setActive(Boolean.TRUE);
        supplier.setProfit(new BigDecimal("0.00"));
        supplier = clientRepository.save(supplier);

        consumer = new Client("Consumer", "consumer@email.com", "222");
        consumer.setActive(Boolean.TRUE);
        consumer.setProfit(new BigDecimal("0.00"));
        consumer = clientRepository.save(consumer);

        validOrderRequest = new OrderRequest();
        validOrderRequest.setIdempotencyId(UUID.randomUUID().toString());
        validOrderRequest.setTitle("Test Order");
        validOrderRequest.setSupplierId(supplier.getId());
        validOrderRequest.setConsumerId(consumer.getId());
        validOrderRequest.setPrice(new BigDecimal("500.00"));
    }

    @Test
    void createOrder_shouldCreateOrderAndUpdateClientProfitsWhenRequestIsValid() {
        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();

        Optional<Client> updatedSupplier = clientRepository.findById(supplier.getId());
        assertThat(updatedSupplier).isPresent();
        assertThat(updatedSupplier.get().getProfit()).isEqualTo(new BigDecimal("500.00"));

        Optional<Client> updatedConsumer = clientRepository.findById(consumer.getId());
        assertThat(updatedConsumer).isPresent();
        assertThat(updatedConsumer.get().getProfit()).isEqualTo(new BigDecimal("-500.00"));
    }

    @Test
    void createOrder_shouldFail_whenIdempotencyCheckReturnsFalse() {
        orderService.createOrder(validOrderRequest);

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("This order is already being processed.");
    }

    @Test
    void createOrder_shouldFail_whenPriceIsZeroOrNegative() {
        validOrderRequest.setPrice(BigDecimal.ZERO);

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Price must be greater than zero");
    }

    @Test
    void createOrder_shouldFailWhenOrderWithSameBusinessKeyExists() {
        orderService.createOrder(validOrderRequest);
        validOrderRequest.setIdempotencyId(UUID.randomUUID().toString());

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Order already exists with this business key");
    }

    @Test
    void createOrder_shouldFailWhenSupplierDoesNotExist() {
        validOrderRequest.setSupplierId(999L);

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Supplier not found in DB.");
    }

    @Test
    void createOrder_shouldFailWhenConsumerDoesNotExist() {
        validOrderRequest.setConsumerId(999L);

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Consumer not found in DB.");
    }

    @Test
    void createOrder_shouldFail_whenConsumerIsInactive() {
        consumer.setActive(false);
        clientRepository.save(consumer);

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Consumer is inactive.");
    }

    @Test
    void createOrder_shouldFailWhenConsumerProfitLimitIsExceeded() {
        consumer.setProfit(new BigDecimal("-900.00"));
        clientRepository.save(consumer);

        AppResponse<Long> response = orderService.createOrder(validOrderRequest);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Profit limit exceeded.");
    }

    @Test
    void getOrderById_shouldReturnOrderWhenOrderExists() {
        orderService.createOrder(validOrderRequest);
        Order savedOrder = orderRepository.findAll().get(0);

        AppResponse<OrderResponse> response = orderService.getOrderById(savedOrder.getId());

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();
        assertThat(response.data().getTitle()).isEqualTo(savedOrder.getTitle());
    }

    @Test
    void getOrderById_shouldReturnFailureWhenOrderDoesNotExist() {
        Long nonExistentOrderId = 999L;

        AppResponse<OrderResponse> response = orderService.getOrderById(nonExistentOrderId);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("Order not found in DB.");
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        orderService.createOrder(validOrderRequest);

        OrderRequest secondOrderRequest = new OrderRequest();
        secondOrderRequest.setIdempotencyId(UUID.randomUUID().toString());
        secondOrderRequest.setTitle("Second Test Order");
        secondOrderRequest.setSupplierId(supplier.getId());
        secondOrderRequest.setConsumerId(consumer.getId());
        secondOrderRequest.setPrice(new BigDecimal("100.00"));
        orderService.createOrder(secondOrderRequest);

        AppResponse<List<OrderResponse>> response = orderService.getAllOrders();

        assertThat(response.success()).isTrue();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().stream().map(OrderResponse::getTitle))
                .containsExactlyInAnyOrder("Test Order", "Second Test Order");
    }
}
