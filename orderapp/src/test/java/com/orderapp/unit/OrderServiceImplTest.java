package com.orderapp.unit;

import com.orderapp.model.Client;
import com.orderapp.model.Order;
import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.OrderRequest;
import com.orderapp.model.dto.OrderResponse;
import com.orderapp.repository.ClientRepository;
import com.orderapp.repository.OrderRepository;
import com.orderapp.service.IdempotencyService;
import com.orderapp.service.impl.OrderServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;
    private Client supplier;
    private Client consumer;
    private Order order;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "totalBenefit", "-1000");

        orderRequest = new OrderRequest();
        orderRequest.setTitle("Test Order");
        orderRequest.setPrice(new BigDecimal("100"));
        orderRequest.setSupplierId(1L);
        orderRequest.setConsumerId(2L);

        supplier = new Client();
        supplier.setId(1L);
        supplier.setProfit(new BigDecimal("500"));

        consumer = new Client();
        consumer.setId(2L);
        consumer.setProfit(new BigDecimal("2000"));
        consumer.setActive(true);

        order = new Order();
        order.setId(1L);
        order.setTitle("Test Order");
        order.setSupplier(supplier);
        order.setConsumer(consumer);
    }

    @Test
    void createOrder_success() {
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(supplier));
        when(clientRepository.findJustClientById(2L)).thenReturn(Optional.of(consumer));
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertTrue(response.success());
        assertNotNull(response.data());
        assertEquals(1L, response.data());
        assertEquals("success", response.message());

        verify(idempotencyService, times(1)).checkAndSetIdempotencyId(any(OrderRequest.class));
        verify(clientRepository, times(1)).findJustClientById(1L);
        verify(clientRepository, times(1)).findJustClientById(2L);
        verify(clientRepository, times(2)).save(any(Client.class));
        verify(orderRepository, times(1)).existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_idempotencyCheckFailed() {
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(false);

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("This order is already being processed.", response.message());

        verify(idempotencyService, times(1)).checkAndSetIdempotencyId(any(OrderRequest.class));
        verify(orderRepository, never()).existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong());
    }

    @Test
    void createOrder_priceIsZero() {
        orderRequest.setPrice(BigDecimal.ZERO);
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Price must be greater than zero", response.message());

        verify(orderRepository, never()).existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong());
    }

    @Test
    void createOrder_orderAlreadyExists() {
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(true);

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Order already exists with this business key", response.message());

        verify(orderRepository, times(1)).existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong());
    }

    @Test
    void createOrder_supplierNotFound() {
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.empty());

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Supplier not found in DB.", response.message());

        verify(clientRepository, times(1)).findJustClientById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_consumerNotFound() {
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(supplier));
        when(clientRepository.findJustClientById(2L)).thenReturn(Optional.empty());

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Consumer not found in DB.", response.message());

        verify(clientRepository, times(2)).findJustClientById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_consumerIsInactive() {
        consumer.setActive(false);
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(supplier));
        when(clientRepository.findJustClientById(2L)).thenReturn(Optional.of(consumer));

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Consumer is inactive.", response.message());

        verify(clientRepository, times(2)).findJustClientById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_profitLimitExceeded() {
        consumer.setProfit(new BigDecimal("-950"));
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(supplier));
        when(clientRepository.findJustClientById(2L)).thenReturn(Optional.of(consumer));

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Profit limit exceeded.", response.message());

        verify(clientRepository, times(2)).findJustClientById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_consumerBecomesInactiveDuringProcessing() {
        when(idempotencyService.checkAndSetIdempotencyId(any(OrderRequest.class))).thenReturn(true);
        when(clientRepository.findJustClientById(1L)).thenReturn(Optional.of(supplier));
        when(clientRepository.findJustClientById(2L)).thenReturn(Optional.of(consumer));
        when(orderRepository.existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong())).thenReturn(false);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                consumer.setActive(false);
                return null;
            }
        }).when(entityManager).refresh(any(Client.class));

        AppResponse<Long> response = orderService.createOrder(orderRequest);

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Consumer became inactive during processing.", response.message());

        verify(clientRepository, times(1)).findJustClientById(1L);
        verify(clientRepository, times(1)).findJustClientById(2L);
        verify(orderRepository, times(1)).existsByTitleAndSupplierIdAndConsumerId(anyString(), anyLong(), anyLong());
        verify(entityManager, times(1)).refresh(any(Client.class));
        verify(clientRepository, never()).save(any(Client.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AppResponse<OrderResponse> response = orderService.getOrderById(1L);

        assertTrue(response.success());
        assertNotNull(response.data());
        assertEquals("Test Order", response.data().getTitle());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        AppResponse<OrderResponse> response = orderService.getOrderById(99L);

        assertFalse(response.success());
        assertEquals("Order not found in DB.", response.message());

        verify(orderRepository, times(1)).findById(99L);
    }

    @Test
    void getAllOrders_success() {
        Client supplier2 = new Client();
        supplier2.setId(1L);
        supplier2.setProfit(new BigDecimal("500"));

        Client consumer2 = new Client();
        consumer2.setId(2L);
        consumer2.setProfit(new BigDecimal("2000"));
        consumer2.setActive(true);

        Order order2 = new Order();
        order2.setId(1L);
        order2.setTitle("Test Order");
        order2.setSupplier(supplier2);
        order2.setConsumer(consumer2);

        List<Order> orders = List.of(order, order2);
        when(orderRepository.findAll()).thenReturn(orders);

        AppResponse<List<OrderResponse>> response = orderService.getAllOrders();

        assertTrue(response.success());
        assertFalse(response.data().isEmpty());
        assertEquals(2, response.data().size());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllOrders_emptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        AppResponse<List<OrderResponse>> response = orderService.getAllOrders();

        assertTrue(response.success());
        assertTrue(response.data().isEmpty());

        verify(orderRepository, times(1)).findAll();
    }
}
