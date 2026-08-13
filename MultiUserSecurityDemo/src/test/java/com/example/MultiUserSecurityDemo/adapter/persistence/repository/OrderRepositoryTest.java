package com.example.MultiUserSecurityDemo.adapter.persistence.repository;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.OrderEntity;
import com.example.MultiUserSecurityDemo.adapter.persistence.entity.OrderItemEntity;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private OrderEntity createOrder(
            String email,
            String status) {

        return OrderEntity.builder()
                .userEmail(email)
                .userName("Test User")
                .totalAmount(new BigDecimal("1999.98"))
                .status(status)
                .build();
    }


    private OrderItemEntity createItem(
            OrderEntity order,
            Long productId,
            String productName,
            int quantity,
            BigDecimal price) {

        return OrderItemEntity.builder()
                .order(order)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .price(price)
                .subtotal(
                        price.multiply(
                                BigDecimal.valueOf(quantity)))
                .build();
    }


    @Test
    @DisplayName("Should find orders by user email ordered by newest first")
    void findByUserEmailOrderByCreatedAtDesc_shouldReturnNewestFirst() {

        // Arrange
        OrderEntity older =
                createOrder(
                        "user@test.com",
                        "PENDING");

        OrderEntity newer =
                createOrder(
                        "user@test.com",
                        "SHIPPED");

        OrderEntity otherUser =
                createOrder(
                        "other@test.com",
                        "PENDING");

        orderRepository.saveAndFlush(older);

        orderRepository.saveAndFlush(newer);

        orderRepository.saveAndFlush(otherUser);

        /*
         * @PrePersist generates createdAt automatically.
         * We update the persisted timestamps directly so that
         * the ORDER BY behavior is deterministic.
         */
        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 1, 1, 10, 0)),
                older.getId());

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 1, 2, 10, 0)),
                newer.getId());

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 1, 3, 10, 0)),
                otherUser.getId());

        // Clear persistence context so query hits database state.
        orderRepository.flush();

        // Act
        List<OrderEntity> result =
                orderRepository.findByUserEmailOrderByCreatedAtDesc(
                        "user@test.com");

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).getId())
                .isEqualTo(newer.getId());

        assertThat(result.get(1).getId())
                .isEqualTo(older.getId());
    }


    @Test
    @DisplayName("Should return all orders ordered by newest first")
    void findAllByOrderByCreatedAtDesc_shouldReturnNewestFirst() {

        // Arrange
        OrderEntity first =
                createOrder(
                        "first@test.com",
                        "PENDING");

        OrderEntity second =
                createOrder(
                        "second@test.com",
                        "SHIPPED");

        orderRepository.saveAndFlush(first);
        orderRepository.saveAndFlush(second);

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 2, 1, 10, 0)),
                first.getId());

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 2, 2, 10, 0)),
                second.getId());

        // Act
        List<OrderEntity> result =
                orderRepository.findAllByOrderByCreatedAtDesc();

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).getId())
                .isEqualTo(second.getId());

        assertThat(result.get(1).getId())
                .isEqualTo(first.getId());
    }


    @Test
    @DisplayName("Should find orders by status ordered by newest first")
    void findByStatusOrderByCreatedAtDesc_shouldReturnMatchingOrders() {

        // Arrange
        OrderEntity older =
                createOrder(
                        "older@test.com",
                        "PENDING");

        OrderEntity newer =
                createOrder(
                        "newer@test.com",
                        "PENDING");

        OrderEntity shipped =
                createOrder(
                        "shipped@test.com",
                        "SHIPPED");

        orderRepository.saveAndFlush(older);
        orderRepository.saveAndFlush(newer);
        orderRepository.saveAndFlush(shipped);

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 3, 1, 10, 0)),
                older.getId());

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 3, 2, 10, 0)),
                newer.getId());

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(
                                2026, 3, 3, 10, 0)),
                shipped.getId());

        // Act
        List<OrderEntity> result =
                orderRepository.findByStatusOrderByCreatedAtDesc(
                        "PENDING");

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).getId())
                .isEqualTo(newer.getId());

        assertThat(result.get(1).getId())
                .isEqualTo(older.getId());
    }


    @Test
    @DisplayName("Should return empty when no orders match user email")
    void findByUserEmailOrderByCreatedAtDesc_shouldReturnEmpty_whenUserHasNoOrders() {

        // Act
        List<OrderEntity> result =
                orderRepository.findByUserEmailOrderByCreatedAtDesc(
                        "missing@test.com");

        // Assert
        assertThat(result)
                .isEmpty();
    }


    @Test
    @DisplayName("Should find order by ID")
    void findById_shouldReturnOrder_whenIdExists() {

        // Arrange
        OrderEntity order =
                createOrder(
                        "user@test.com",
                        "PENDING");

        orderRepository.saveAndFlush(order);

        // Act
        Optional<OrderEntity> result =
                orderRepository.findById(order.getId());

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get().getId())
                .isEqualTo(order.getId());

        assertThat(result.get().getUserEmail())
                .isEqualTo("user@test.com");
    }


    @Test
    @DisplayName("Should return empty when order ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {

        // Act
        Optional<OrderEntity> result =
                orderRepository.findById(999999L);

        // Assert
        assertThat(result)
                .isEmpty();
    }


    @Test
    @DisplayName("Should load order items with order when using findById")
    void findById_shouldLoadItemsWithEntityGraph() {

        // Arrange
        OrderEntity order =
                createOrder(
                        "user@test.com",
                        "PENDING");

        OrderItemEntity item =
                createItem(
                        order,
                        101L,
                        "Laptop",
                        2,
                        new BigDecimal("999.99"));

        order.getItems().add(item);

        orderRepository.saveAndFlush(order);

        // Clear persistence context so entity is loaded again.
        orderRepository.flush();

        // Act
        Optional<OrderEntity> result =
                orderRepository.findById(order.getId());

        // Assert
        assertThat(result)
                .isPresent();

        OrderEntity foundOrder = result.get();

        assertThat(Hibernate.isInitialized(foundOrder.getItems()))
                .isTrue();

        assertThat(foundOrder.getItems())
                .hasSize(1);

        assertThat(foundOrder.getItems().get(0).getProductId())
                .isEqualTo(101L);

        assertThat(foundOrder.getItems().get(0).getProductName())
                .isEqualTo("Laptop");

        assertThat(foundOrder.getItems().get(0).getQuantity())
                .isEqualTo(2);
    }
}