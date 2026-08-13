package com.example.MultiUserSecurityDemo.adapter.persistence;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.ProductEntity;
import com.example.MultiUserSecurityDemo.adapter.persistence.mapper.ProductMapper;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.ProductRepository;
import com.example.MultiUserSecurityDemo.domain.model.Product;

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
@DisplayName("ProductPersistenceAdapter Tests")
class ProductPersistenceAdapterTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductPersistenceAdapter adapter;


    @Test
    @DisplayName("Should map, save and return domain product")
    void save_shouldMapSaveAndReturnDomainProduct() {

        // Arrange
        Product product =
                new Product();

        ProductEntity entity =
                new ProductEntity();

        ProductEntity savedEntity =
                new ProductEntity();

        Product savedProduct =
                new Product();

        when(mapper.toEntity(
                product,
                product.getOwnerType()))
                .thenReturn(entity);

        when(repository.save(entity))
                .thenReturn(savedEntity);

        when(mapper.toDomain(savedEntity))
                .thenReturn(savedProduct);

        // Act
        Product result =
                adapter.save(product);

        // Assert
        assertThat(result)
                .isSameAs(savedProduct);

        verify(mapper)
                .toEntity(
                        product,
                        product.getOwnerType());

        verify(repository)
                .save(entity);

        verify(mapper)
                .toDomain(savedEntity);
    }


    @Test
    @DisplayName("Should return domain product when ID exists")
    void findById_shouldReturnProduct_whenIdExists() {

        // Arrange
        Long id = 10L;

        ProductEntity entity =
                new ProductEntity();

        Product product =
                new Product();

        when(repository.findById(id))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(product);

        // Act
        Optional<Product> result =
                adapter.findById(id);

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get())
                .isSameAs(product);

        verify(repository)
                .findById(id);

        verify(mapper)
                .toDomain(entity);
    }


    @Test
    @DisplayName("Should return empty when product ID does not exist")
    void findById_shouldReturnEmpty_whenProductDoesNotExist() {

        // Arrange
        Long id = 999L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Act
        Optional<Product> result =
                adapter.findById(id);

        // Assert
        assertThat(result)
                .isEmpty();

        verify(repository)
                .findById(id);

        verifyNoInteractions(mapper);
    }


    @Test
    @DisplayName("Should return all products mapped to domain objects")
    void findAll_shouldReturnAllProducts() {

        // Arrange
        ProductEntity entity1 =
                new ProductEntity();

        ProductEntity entity2 =
                new ProductEntity();

        Product product1 =
                new Product();

        Product product2 =
                new Product();

        when(repository.findAll())
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(product1);

        when(mapper.toDomain(entity2))
                .thenReturn(product2);

        // Act
        List<Product> result =
                adapter.findAll();

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        product1,
                        product2);

        verify(repository)
                .findAll();

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }


    @Test
    @DisplayName("Should return products belonging to category")
    void findByCategory_shouldReturnMatchingProducts() {

        // Arrange
        String category = "Electronics";

        ProductEntity entity1 =
                new ProductEntity();

        ProductEntity entity2 =
                new ProductEntity();

        Product product1 =
                new Product();

        Product product2 =
                new Product();

        when(repository.findByCategory(category))
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(product1);

        when(mapper.toDomain(entity2))
                .thenReturn(product2);

        // Act
        List<Product> result =
                adapter.findByCategory(category);

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        product1,
                        product2);

        verify(repository)
                .findByCategory(category);

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }


    @Test
    @DisplayName("Should return products based on active status")
    void findByIsActive_shouldReturnMatchingProducts() {

        // Arrange
        boolean active = true;

        ProductEntity entity1 =
                new ProductEntity();

        ProductEntity entity2 =
                new ProductEntity();

        Product product1 =
                new Product();

        Product product2 =
                new Product();

        when(repository.findByIsActive(active))
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(product1);

        when(mapper.toDomain(entity2))
                .thenReturn(product2);

        // Act
        List<Product> result =
                adapter.findByIsActive(active);

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        product1,
                        product2);

        verify(repository)
                .findByIsActive(active);

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }


    @Test
    @DisplayName("Should delete product by ID")
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


    @Test
    @DisplayName("Should return whether product exists by ID")
    void existsById_shouldReturnRepositoryResult() {

        // Arrange
        Long id = 10L;

        when(repository.existsById(id))
                .thenReturn(true);

        // Act
        boolean result =
                adapter.existsById(id);

        // Assert
        assertThat(result)
                .isTrue();

        verify(repository)
                .existsById(id);

        verifyNoInteractions(mapper);
    }
}
