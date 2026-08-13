package com.example.MultiUserSecurityDemo.adapter.persistence.repository;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;


    private ProductEntity createProduct(
            String name,
            String category,
            boolean active) {

        return ProductEntity.builder()
                .name(name)
                .description(name + " description")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .category(category)
                .imageUrl("https://example.com/image.jpg")
                .isActive(active)
                .ownerType("TYPE1")
                .createdBy("admin@test.com")
                .build();
    }


    @Test
    @DisplayName("Should find products by category")
    void findByCategory_shouldReturnMatchingProducts() {

        // Arrange
        ProductEntity laptop =
                createProduct("Laptop", "Electronics", true);

        ProductEntity phone =
                createProduct("Phone", "Electronics", true);

        ProductEntity chair =
                createProduct("Chair", "Furniture", true);

        productRepository.save(laptop);
        productRepository.save(phone);
        productRepository.save(chair);

        // Act
        List<ProductEntity> result =
                productRepository.findByCategory("Electronics");

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(ProductEntity::getName)
                .containsExactlyInAnyOrder(
                        "Laptop",
                        "Phone"
                );
    }


    @Test
    @DisplayName("Should return empty list when category does not exist")
    void findByCategory_shouldReturnEmpty_whenCategoryDoesNotExist() {

        // Act
        List<ProductEntity> result =
                productRepository.findByCategory("NonExistingCategory");

        // Assert
        assertThat(result)
                .isEmpty();
    }


    @Test
    @DisplayName("Should find active products")
    void findByIsActive_shouldReturnActiveProducts() {

        // Arrange
        ProductEntity activeProduct =
                createProduct("Active Product", "Electronics", true);

        ProductEntity inactiveProduct =
                createProduct("Inactive Product", "Electronics", false);

        productRepository.save(activeProduct);
        productRepository.save(inactiveProduct);

        // Act
        List<ProductEntity> result =
                productRepository.findByIsActive(true);

        // Assert
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getName())
                .isEqualTo("Active Product");

        assertThat(result.get(0).getIsActive())
                .isTrue();
    }


    @Test
    @DisplayName("Should find inactive products")
    void findByIsActive_shouldReturnInactiveProducts() {

        // Arrange
        ProductEntity activeProduct =
                createProduct("Active Product", "Electronics", true);

        ProductEntity inactiveProduct =
                createProduct("Inactive Product", "Electronics", false);

        productRepository.save(activeProduct);
        productRepository.save(inactiveProduct);

        // Act
        List<ProductEntity> result =
                productRepository.findByIsActive(false);

        // Assert
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getName())
                .isEqualTo("Inactive Product");

        assertThat(result.get(0).getIsActive())
                .isFalse();
    }


    @Test
    @DisplayName("Should find products by category and active status")
    void findByCategoryAndIsActive_shouldReturnMatchingProducts() {

        // Arrange
        ProductEntity activeElectronics =
                createProduct(
                        "Active Laptop",
                        "Electronics",
                        true);

        ProductEntity inactiveElectronics =
                createProduct(
                        "Inactive Laptop",
                        "Electronics",
                        false);

        ProductEntity activeFurniture =
                createProduct(
                        "Active Chair",
                        "Furniture",
                        true);

        productRepository.save(activeElectronics);
        productRepository.save(inactiveElectronics);
        productRepository.save(activeFurniture);

        // Act
        List<ProductEntity> result =
                productRepository.findByCategoryAndIsActive(
                        "Electronics",
                        true);

        // Assert
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getName())
                .isEqualTo("Active Laptop");

        assertThat(result.get(0).getCategory())
                .isEqualTo("Electronics");

        assertThat(result.get(0).getIsActive())
                .isTrue();
    }


    @Test
    @DisplayName("Should return empty when category and active status do not match")
    void findByCategoryAndIsActive_shouldReturnEmpty_whenNoMatch() {

        // Arrange
        ProductEntity product =
                createProduct(
                        "Laptop",
                        "Electronics",
                        false);

        productRepository.save(product);

        // Act
        List<ProductEntity> result =
                productRepository.findByCategoryAndIsActive(
                        "Electronics",
                        true);

        // Assert
        assertThat(result)
                .isEmpty();
    }
}