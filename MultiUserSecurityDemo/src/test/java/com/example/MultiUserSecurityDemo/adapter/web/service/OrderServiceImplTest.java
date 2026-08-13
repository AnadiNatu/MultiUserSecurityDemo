package com.example.MultiUserSecurityDemo.adapter.web.service;

import com.example.MultiUserSecurityDemo.adapter.web.dto.order.OrderRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.order.OrderResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.impl.OrderServiceImpl;
import com.example.MultiUserSecurityDemo.domain.model.Order;
import com.example.MultiUserSecurityDemo.domain.model.Product;
import com.example.MultiUserSecurityDemo.domain.port.OrderPort;
import com.example.MultiUserSecurityDemo.domain.port.ProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock
    private OrderPort orderPort;

    @Mock
    private ProductPort productPort;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order with multiple products and calculate total")
        void createOrder_shouldCreateOrderAndCalculateTotal() {

            // Arrange
            Product laptop = createProduct(
                    1L,
                    "Laptop",
                    new BigDecimal("1000.00")
            );

            Product mouse = createProduct(
                    2L,
                    "Mouse",
                    new BigDecimal("50.00")
            );

            OrderRequest.OrderItemRequest laptopItem =
                    createOrderItemRequest(1L, 2);

            OrderRequest.OrderItemRequest mouseItem =
                    createOrderItemRequest(2L, 3);

            OrderRequest request =
                    OrderRequest.builder()
                            .items(List.of(laptopItem, mouseItem))
                            .build();

            when(productPort.findById(1L))
                    .thenReturn(Optional.of(laptop));

            when(productPort.findById(2L))
                    .thenReturn(Optional.of(mouse));

            when(orderPort.save(any(Order.class)))
                    .thenAnswer(invocation -> {
                        Order order = invocation.getArgument(0);
                        order.setId(100L);
                        return order;
                    });

            // Act
            OrderResponse result =
                    orderService.createOrder(
                            request,
                            "user@test.com",
                            "Test User"
                    );

            // Assert
            assertThat(result)
                    .isNotNull();

            assertThat(result.getId())
                    .isEqualTo("100");

            assertThat(result.getUserId())
                    .isEqualTo("user@test.com");

            assertThat(result.getUserName())
                    .isEqualTo("Test User");

            assertThat(result.getStatus())
                    .isEqualTo("PENDING");

            assertThat(result.getTotalAmount())
                    .isEqualByComparingTo("2150.00");

            assertThat(result.getItems())
                    .hasSize(2);

            assertThat(result.getItems().get(0).getProductId())
                    .isEqualTo("1");

            assertThat(result.getItems().get(0).getProductName())
                    .isEqualTo("Laptop");

            assertThat(result.getItems().get(0).getQuantity())
                    .isEqualTo(2);

            assertThat(result.getItems().get(0).getPrice())
                    .isEqualByComparingTo("1000.00");

            assertThat(result.getItems().get(0).getSubtotal())
                    .isEqualByComparingTo("2000.00");

            assertThat(result.getItems().get(1).getProductId())
                    .isEqualTo("2");

            assertThat(result.getItems().get(1).getSubtotal())
                    .isEqualByComparingTo("150.00");

            verify(productPort)
                    .findById(1L);

            verify(productPort)
                    .findById(2L);

            verify(orderPort)
                    .save(any(Order.class));
        }

        @Test
        @DisplayName("Should skip product when product does not exist")
        void createOrder_shouldSkipMissingProduct() {

            // Arrange
            Product laptop = createProduct(
                    1L,
                    "Laptop",
                    new BigDecimal("1000.00")
            );

            OrderRequest.OrderItemRequest existingItem =
                    createOrderItemRequest(1L, 2);

            OrderRequest.OrderItemRequest missingItem =
                    createOrderItemRequest(999L, 5);

            OrderRequest request =
                    OrderRequest.builder()
                            .items(List.of(
                                    existingItem,
                                    missingItem
                            ))
                            .build();

            when(productPort.findById(1L))
                    .thenReturn(Optional.of(laptop));

            when(productPort.findById(999L))
                    .thenReturn(Optional.empty());

            when(orderPort.save(any(Order.class)))
                    .thenAnswer(invocation -> {
                        Order order = invocation.getArgument(0);
                        order.setId(101L);
                        return order;
                    });

            // Act
            OrderResponse result =
                    orderService.createOrder(
                            request,
                            "user@test.com",
                            "Test User"
                    );

            // Assert
            assertThat(result.getTotalAmount())
                    .isEqualByComparingTo("2000.00");

            assertThat(result.getItems())
                    .hasSize(1);

            assertThat(result.getItems().get(0).getProductId())
                    .isEqualTo("1");

            verify(productPort)
                    .findById(1L);

            verify(productPort)
                    .findById(999L);

            verify(orderPort)
                    .save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Order Retrieval Tests")
    class OrderRetrievalTests {

        @Test
        @DisplayName("Should return all orders")
        void getAllOrders_shouldReturnAllOrders() {

            // Arrange
            Order order1 = createOrder(
                    1L,
                    "user1@test.com",
                    "User One",
                    "PENDING",
                    new BigDecimal("100.00")
            );

            Order order2 = createOrder(
                    2L,
                    "user2@test.com",
                    "User Two",
                    "COMPLETED",
                    new BigDecimal("250.00")
            );

            when(orderPort.findAll())
                    .thenReturn(List.of(order1, order2));

            // Act
            List<OrderResponse> result =
                    orderService.getAllOrders();

            // Assert
            assertThat(result)
                    .hasSize(2);

            assertThat(result.get(0).getId())
                    .isEqualTo("1");

            assertThat(result.get(1).getId())
                    .isEqualTo("2");

            verify(orderPort)
                    .findAll();
        }

        @Test
        @DisplayName("Should return orders belonging to user")
        void getOrdersByUser_shouldReturnUserOrders() {

            // Arrange
            String email = "user@test.com";

            Order order1 = createOrder(
                    10L,
                    email,
                    "Test User",
                    "PENDING",
                    new BigDecimal("100.00")
            );

            Order order2 = createOrder(
                    11L,
                    email,
                    "Test User",
                    "COMPLETED",
                    new BigDecimal("200.00")
            );

            when(orderPort.findByUserEmail(email))
                    .thenReturn(List.of(order1, order2));

            // Act
            List<OrderResponse> result =
                    orderService.getOrdersByUser(email);

            // Assert
            assertThat(result)
                    .hasSize(2);

            assertThat(result)
                    .allMatch(
                            order -> order.getUserId().equals(email)
                    );

            verify(orderPort)
                    .findByUserEmail(email);
        }
    }


    @Nested
    @DisplayName("Order Status Tests")
    class OrderStatusTests {

        @Test
        @DisplayName("Should update order status to COMPLETED")
        void updateOrderStatus_shouldUpdateValidStatus() {

            // Arrange
            Long id = 20L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "PENDING",
                    new BigDecimal("500.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            when(orderPort.save(order))
                    .thenReturn(order);

            // Act
            OrderResponse result =
                    orderService.updateOrderStatus(
                            id,
                            "completed",
                            "admin@test.com"
                    );

            // Assert
            assertThat(order.getStatus())
                    .isEqualTo("COMPLETED");

            assertThat(order.getUpdatedAt())
                    .isNotNull();

            assertThat(result.getStatus())
                    .isEqualTo("COMPLETED");

            verify(orderPort)
                    .findById(id);

            verify(orderPort)
                    .save(order);
        }

        @Test
        @DisplayName("Should reject invalid order status")
        void updateOrderStatus_shouldThrowForInvalidStatus() {

            // Arrange
            Long id = 21L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "PENDING",
                    new BigDecimal("100.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.updateOrderStatus(
                            id,
                            "SHIPPED",
                            "admin@test.com"
                    )
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid status: SHIPPED");

            verify(orderPort)
                    .findById(id);

            verify(orderPort, never())
                    .save(any(Order.class));
        }
    }


    @Nested
    @DisplayName("Single Order Tests")
    class SingleOrderTests {

        @Test
        @DisplayName("Should return order by ID")
        void getOrderById_shouldReturnOrder() {

            // Arrange
            Long id = 30L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "PENDING",
                    new BigDecimal("300.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act
            OrderResponse result =
                    orderService.getOrderById(id);

            // Assert
            assertThat(result)
                    .isNotNull();

            assertThat(result.getId())
                    .isEqualTo("30");

            assertThat(result.getUserId())
                    .isEqualTo("user@test.com");

            assertThat(result.getTotalAmount())
                    .isEqualByComparingTo("300.00");

            verify(orderPort)
                    .findById(id);
        }

        @Test
        @DisplayName("Should throw when order does not exist")
        void getOrderById_shouldThrowWhenOrderNotFound() {

            // Arrange
            Long id = 999L;

            when(orderPort.findById(id))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.getOrderById(id)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Order not found: 999");

            verify(orderPort)
                    .findById(id);
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel own pending order")
        void cancelOrder_shouldCancelPendingOwnOrder() {

            // Arrange
            Long id = 40L;
            String email = "user@test.com";

            Order order = createOrder(
                    id,
                    email,
                    "Test User",
                    "PENDING",
                    new BigDecimal("400.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            when(orderPort.save(order))
                    .thenReturn(order);

            // Act
            OrderResponse result =
                    orderService.cancelOrder(
                            id,
                            email
                    );

            // Assert
            assertThat(order.getStatus())
                    .isEqualTo("CANCELLED");

            assertThat(order.getUpdatedAt())
                    .isNotNull();

            assertThat(result.getStatus())
                    .isEqualTo("CANCELLED");

            verify(orderPort)
                    .save(order);
        }

        @Test
        @DisplayName("Should reject cancellation of another user's order")
        void cancelOrder_shouldRejectAnotherUsersOrder() {

            // Arrange
            Long id = 41L;

            Order order = createOrder(
                    id,
                    "owner@test.com",
                    "Owner",
                    "PENDING",
                    new BigDecimal("400.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.cancelOrder(
                            id,
                            "attacker@test.com"
                    )
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(
                            "You cannot cancel another user's order."
                    );

            verify(orderPort, never())
                    .save(any(Order.class));
        }

        @Test
        @DisplayName("Should reject cancellation of non-pending order")
        void cancelOrder_shouldRejectCompletedOrder() {

            // Arrange
            Long id = 42L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "COMPLETED",
                    new BigDecimal("400.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.cancelOrder(
                            id,
                            "user@test.com"
                    )
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(
                            "Only pending orders can be cancelled."
                    );

            verify(orderPort, never())
                    .save(any(Order.class));
        }
    }


    @Nested
    @DisplayName("Delete Own Order Tests")
    class DeleteOwnOrderTests {

        @Test
        @DisplayName("Should delete user's cancelled order")
        void deleteOwnOrder_shouldDeleteCancelledOwnOrder() {

            // Arrange
            Long id = 50L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "CANCELLED",
                    new BigDecimal("200.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act
            orderService.deleteOwnOrder(
                    id,
                    "user@test.com"
            );

            // Assert
            verify(orderPort)
                    .findById(id);

            verify(orderPort)
                    .deleteById(id);
        }

        @Test
        @DisplayName("Should reject deletion by another user")
        void deleteOwnOrder_shouldRejectAnotherUser() {

            // Arrange
            Long id = 51L;

            Order order = createOrder(
                    id,
                    "owner@test.com",
                    "Owner",
                    "CANCELLED",
                    new BigDecimal("200.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.deleteOwnOrder(
                            id,
                            "other@test.com"
                    )
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Access denied");

            verify(orderPort, never())
                    .deleteById(id);
        }

        @Test
        @DisplayName("Should reject deletion of non-cancelled order")
        void deleteOwnOrder_shouldRejectNonCancelledOrder() {

            // Arrange
            Long id = 52L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "PENDING",
                    new BigDecimal("200.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.deleteOwnOrder(
                            id,
                            "user@test.com"
                    )
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(
                            "Only cancelled orders can be deleted."
                    );

            verify(orderPort, never())
                    .deleteById(id);
        }
    }


    @Nested
    @DisplayName("Admin Delete Tests")
    class AdminDeleteTests {

        @Test
        @DisplayName("Should allow admin to delete existing order")
        void deleteOrder_shouldDeleteExistingOrder() {

            // Arrange
            Long id = 60L;

            Order order = createOrder(
                    id,
                    "user@test.com",
                    "Test User",
                    "PENDING",
                    new BigDecimal("100.00")
            );

            when(orderPort.findById(id))
                    .thenReturn(Optional.of(order));

            // Act
            orderService.deleteOrder(id);

            // Assert
            verify(orderPort)
                    .findById(id);

            verify(orderPort)
                    .deleteById(id);
        }


        @Test
        @DisplayName("Should throw when admin deletes missing order")
        void deleteOrder_shouldThrowWhenOrderDoesNotExist() {

            // Arrange
            Long id = 999L;

            when(orderPort.findById(id))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() ->
                    orderService.deleteOrder(id)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Order not found");

            verify(orderPort, never())
                    .deleteById(id);
        }
    }


    @Nested
    @DisplayName("Status and User Filter Tests")
    class StatusAndUserFilterTests {

        @Test
        @DisplayName("Should return orders matching status ignoring case")
        void getOrdersByStatus_shouldFilterByStatus() {

            // Arrange
            Order pending =
                    createOrder(
                            70L,
                            "user1@test.com",
                            "User One",
                            "PENDING",
                            new BigDecimal("100.00")
                    );

            Order completed =
                    createOrder(
                            71L,
                            "user2@test.com",
                            "User Two",
                            "COMPLETED",
                            new BigDecimal("200.00")
                    );

            Order anotherPending =
                    createOrder(
                            72L,
                            "user3@test.com",
                            "User Three",
                            "PENDING",
                            new BigDecimal("300.00")
                    );

            when(orderPort.findAll())
                    .thenReturn(
                            List.of(
                                    pending,
                                    completed,
                                    anotherPending
                            )
                    );

            // Act
            List<OrderResponse> result =
                    orderService.getOrdersByStatus("pending");

            // Assert
            assertThat(result)
                    .hasSize(2);

            assertThat(result)
                    .allMatch(
                            order ->
                                    order.getStatus()
                                            .equals("PENDING")
                    );

            verify(orderPort)
                    .findAll();
        }

        @Test
        @DisplayName("Should return orders for specified email")
        void getOrdersByUserEmail_shouldReturnMatchingOrders() {

            // Arrange
            String email = "user@test.com";

            Order order1 =
                    createOrder(
                            80L,
                            email,
                            "Test User",
                            "PENDING",
                            new BigDecimal("100.00")
                    );

            Order order2 =
                    createOrder(
                            81L,
                            email,
                            "Test User",
                            "CANCELLED",
                            new BigDecimal("150.00")
                    );

            when(orderPort.findByUserEmail(email))
                    .thenReturn(List.of(order1, order2));

            // Act
            List<OrderResponse> result =
                    orderService.getOrdersByUserEmail(email);

            // Assert
            assertThat(result)
                    .hasSize(2);

            assertThat(result)
                    .allMatch(
                            order ->
                                    order.getUserId()
                                            .equals(email)
                    );

            verify(orderPort)
                    .findByUserEmail(email);
        }
    }


    private Product createProduct(
            Long id,
            String name,
            BigDecimal price) {

        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(100);
        product.setCategory("Test Category");
        product.setIsActive(true);

        return product;
    }


    private OrderRequest.OrderItemRequest createOrderItemRequest(
            Long productId,
            Integer quantity) {

        return OrderRequest.OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }


    private Order createOrder(
            Long id,
            String userEmail,
            String userName,
            String status,
            BigDecimal totalAmount) {

        Order order = new Order();

        order.setId(id);
        order.setUserEmail(userEmail);
        order.setUserName(userName);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(null);

        return order;
    }
}

