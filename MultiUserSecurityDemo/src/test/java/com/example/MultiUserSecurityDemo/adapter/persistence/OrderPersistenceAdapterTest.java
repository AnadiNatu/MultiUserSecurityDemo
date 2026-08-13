package com.example.MultiUserSecurityDemo.adapter.persistence;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.OrderEntity;
import com.example.MultiUserSecurityDemo.adapter.persistence.mapper.OrderMapper;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.OrderRepository;
import com.example.MultiUserSecurityDemo.domain.model.Order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPersistenceAdapter Tests")
class OrderPersistenceAdapterTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderPersistenceAdapter adapter;


    @Test
    @DisplayName("Should map, save and return domain order")
    void save_shouldMapSaveAndReturnDomainOrder() {

        // Arrange
        Order order =
                new Order();

        OrderEntity entity =
                new OrderEntity();

        OrderEntity savedEntity =
                new OrderEntity();

        Order savedOrder =
                new Order();

        when(mapper.toEntity(order))
                .thenReturn(entity);

        when(repository.save(entity))
                .thenReturn(savedEntity);

        when(mapper.toDomain(savedEntity))
                .thenReturn(savedOrder);

        // Act
        Order result =
                adapter.save(order);

        // Assert
        assertThat(result)
                .isSameAs(savedOrder);

        verify(mapper)
                .toEntity(order);

        verify(repository)
                .save(entity);

        verify(mapper)
                .toDomain(savedEntity);
    }


    @Test
    @DisplayName("Should return domain order when ID exists")
    void findById_shouldReturnOrder_whenIdExists() {

        // Arrange
        Long id = 10L;

        OrderEntity entity =
                new OrderEntity();

        Order order =
                new Order();

        when(repository.findById(id))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(order);

        // Act
        Optional<Order> result =
                adapter.findById(id);

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get())
                .isSameAs(order);

        verify(repository)
                .findById(id);

        verify(mapper)
                .toDomain(entity);
    }


    @Test
    @DisplayName("Should return empty when order ID does not exist")
    void findById_shouldReturnEmpty_whenOrderDoesNotExist() {

        // Arrange
        Long id = 999L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Act
        Optional<Order> result =
                adapter.findById(id);

        // Assert
        assertThat(result)
                .isEmpty();

        verify(repository)
                .findById(id);

        verifyNoInteractions(mapper);
    }


    @Test
    @DisplayName("Should return all orders mapped to domain objects")
    void findAll_shouldReturnAllOrders() {

        // Arrange
        OrderEntity entity1 =
                new OrderEntity();

        OrderEntity entity2 =
                new OrderEntity();

        Order order1 =
                new Order();

        Order order2 =
                new Order();

        when(repository.findAll())
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(order1);

        when(mapper.toDomain(entity2))
                .thenReturn(order2);

        // Act
        List<Order> result =
                adapter.findAll();

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        order1,
                        order2);

        verify(repository)
                .findAll();

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }


    @Test
    @DisplayName("Should return orders belonging to user email")
    void findByUserEmail_shouldReturnUserOrders() {

        // Arrange
        String email =
                "user@test.com";

        OrderEntity entity1 =
                new OrderEntity();

        OrderEntity entity2 =
                new OrderEntity();

        Order order1 =
                new Order();

        Order order2 =
                new Order();

        when(repository.findByUserEmailOrderByCreatedAtDesc(email))
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(order1);

        when(mapper.toDomain(entity2))
                .thenReturn(order2);

        // Act
        List<Order> result =
                adapter.findByUserEmail(email);

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        order1,
                        order2);

        verify(repository)
                .findByUserEmailOrderByCreatedAtDesc(email);

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }


    @Test
    @DisplayName("Should delete order by ID")
    void deleteById_shouldDelegateToRepository() {

        // Arrange
        Long id = 10L;

        // Act
        adapter.deleteById(id);

        // Assert
        verify(repository)
                .deleteById(id);

        verifyNoInteractions(mapper);
    }
}
