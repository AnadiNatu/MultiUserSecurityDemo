package com.example.MultiUserSecurityDemo.adapter.web.service;

import com.example.MultiUserSecurityDemo.adapter.persistence.mapper.ProductMapper;
import com.example.MultiUserSecurityDemo.adapter.web.dto.product.ProductRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.product.ProductResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.impl.CloudinaryService;
import com.example.MultiUserSecurityDemo.adapter.web.service.impl.ProductServiceImpl;
import com.example.MultiUserSecurityDemo.domain.model.Product;
import com.example.MultiUserSecurityDemo.domain.port.ProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductPort productPort;

    @Mock
    private ProductMapper mapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product without image")
        void createProduct_shouldCreateProductWithoutImage() {

            // Arrange
            ProductRequest request = createProductRequest(
                    "Laptop",
                    "Gaming laptop",
                    new BigDecimal("1000.00"),
                    10,
                    "Electronics",
                    true
            );

            Product savedProduct = createProduct(
                    1L,
                    "Laptop",
                    new BigDecimal("1000.00"),
                    10,
                    "Electronics",
                    true
            );

            ProductResponse response =
                    createResponse(1L, "Laptop");

            when(productPort.save(any(Product.class)))
                    .thenReturn(savedProduct);

            when(mapper.mapToResponse(savedProduct))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.createProduct(
                            request,
                            null,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .isSameAs(response);

            ArgumentCaptor<Product> captor =
                    ArgumentCaptor.forClass(Product.class);

            verify(productPort)
                    .save(captor.capture());

            Product saved =
                    captor.getValue();

            assertThat(saved.getName())
                    .isEqualTo("Laptop");

            assertThat(saved.getDescription())
                    .isEqualTo("Gaming laptop");

            assertThat(saved.getPrice())
                    .isEqualByComparingTo("1000.00");

            assertThat(saved.getStockQuantity())
                    .isEqualTo(10);

            assertThat(saved.getCategory())
                    .isEqualTo("Electronics");

            assertThat(saved.getIsActive())
                    .isTrue();

            assertThat(saved.getCreatedBy())
                    .isEqualTo("admin@test.com");

            assertThat(saved.getOwnerType())
                    .isEqualTo("ADMIN");

            verifyNoInteractions(cloudinaryService);
        }


        @Test
        @DisplayName("Should upload image and save product again")
        void createProduct_shouldUploadImage_whenImageProvided() {

            // Arrange
            ProductRequest request = createProductRequest(
                    "Phone",
                    "Smart phone",
                    new BigDecimal("700.00"),
                    20,
                    "Electronics",
                    true
            );

            Product firstSaved =
                    createProduct(
                            2L,
                            "Phone",
                            new BigDecimal("700.00"),
                            20,
                            "Electronics",
                            true
                    );

            Product finalSaved =
                    createProduct(
                            2L,
                            "Phone",
                            new BigDecimal("700.00"),
                            20,
                            "Electronics",
                            true
                    );

            ProductResponse response =
                    createResponse(2L, "Phone");

            when(image.isEmpty())
                    .thenReturn(false);

            when(productPort.save(any(Product.class)))
                    .thenReturn(firstSaved, finalSaved);

            when(cloudinaryService.uploadProductImage(
                    image,
                    "2",
                    "ADMIN"
            )).thenReturn("https://cloudinary.com/product2.jpg");

            when(mapper.mapToResponse(finalSaved))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.createProduct(
                            request,
                            image,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .isSameAs(response);

            verify(cloudinaryService)
                    .uploadProductImage(
                            image,
                            "2",
                            "ADMIN"
                    );

            verify(productPort, times(2))
                    .save(any(Product.class));

            assertThat(finalSaved.getImageUrl())
                    .isEqualTo(
                            "https://cloudinary.com/product2.jpg"
                    );
        }
    }

    @Test
    @DisplayName("Should upload image and save product again")
    void createProduct_shouldUploadImage_whenImageProvided() {

        // Arrange
        ProductRequest request = createProductRequest(
                "Phone",
                "Smart phone",
                new BigDecimal("700.00"),
                20,
                "Electronics",
                true
        );

        Product firstSaved =
                createProduct(
                        2L,
                        "Phone",
                        new BigDecimal("700.00"),
                        20,
                        "Electronics",
                        true
                );

        Product finalSaved =
                createProduct(
                        2L,
                        "Phone",
                        new BigDecimal("700.00"),
                        20,
                        "Electronics",
                        true
                );

        ProductResponse response =
                createResponse(2L, "Phone");

        when(image.isEmpty())
                .thenReturn(false);

        when(productPort.save(any(Product.class)))
                .thenReturn(firstSaved, finalSaved);

        when(cloudinaryService.uploadProductImage(
                image,
                "2",
                "ADMIN"
        )).thenReturn("https://cloudinary.com/product2.jpg");

        when(mapper.mapToResponse(finalSaved))
                .thenReturn(response);

        // Act
        ProductResponse result =
                productService.createProduct(
                        request,
                        image,
                        "admin@test.com"
                );

        // Assert
        assertThat(result)
                .isSameAs(response);

        verify(cloudinaryService)
                .uploadProductImage(
                        image,
                        "2",
                        "ADMIN"
                );

        verify(productPort, times(2))
                .save(any(Product.class));

        assertThat(finalSaved.getImageUrl())
                .isEqualTo(
                        "https://cloudinary.com/product2.jpg"
                );
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update existing product without image")
        void updateProduct_shouldUpdateExistingProduct() {

            // Arrange
            Long id = 10L;

            ProductRequest request = createProductRequest(
                    "Updated Laptop",
                    "Updated description",
                    new BigDecimal("1200.00"),
                    15,
                    "Computers",
                    true
            );

            Product existing =
                    createProduct(
                            id,
                            "Old Laptop",
                            new BigDecimal("1000.00"),
                            5,
                            "Electronics",
                            true
                    );

            ProductResponse response =
                    createResponse(id, "Updated Laptop");

            when(productPort.findById(id))
                    .thenReturn(Optional.of(existing));

            when(productPort.save(existing))
                    .thenReturn(existing);

            when(mapper.mapToResponse(existing))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.updateProduct(
                            id,
                            request,
                            null,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .isSameAs(response);

            assertThat(existing.getName())
                    .isEqualTo("Updated Laptop");

            assertThat(existing.getDescription())
                    .isEqualTo("Updated description");

            assertThat(existing.getPrice())
                    .isEqualByComparingTo("1200.00");

            assertThat(existing.getStockQuantity())
                    .isEqualTo(15);

            assertThat(existing.getCategory())
                    .isEqualTo("Computers");

            assertThat(existing.getUpdatedBy())
                    .isEqualTo("admin@test.com");

            verify(productPort)
                    .findById(id);

            verify(productPort)
                    .save(existing);

            verifyNoInteractions(cloudinaryService);
        }

        @Test
        @DisplayName("Should delete old image and upload replacement image")
        void updateProduct_shouldReplaceExistingImage() {

            // Arrange
            Long id = 11L;

            ProductRequest request = createProductRequest(
                    "Updated Phone",
                    "Updated phone",
                    new BigDecimal("800.00"),
                    30,
                    "Electronics",
                    true
            );

            Product existing =
                    createProduct(
                            id,
                            "Phone",
                            new BigDecimal("700.00"),
                            20,
                            "Electronics",
                            true
                    );

            existing.setImageUrl(
                    "https://cloudinary.com/old-image.jpg"
            );

            when(image.isEmpty())
                    .thenReturn(false);

            when(productPort.findById(id))
                    .thenReturn(Optional.of(existing));

            when(cloudinaryService.extractPublicId(
                    existing.getImageUrl()
            )).thenReturn("old-image");

            when(cloudinaryService.uploadProductImage(
                    image,
                    "11",
                    "ADMIN"
            )).thenReturn(
                    "https://cloudinary.com/new-image.jpg"
            );

            when(productPort.save(existing))
                    .thenReturn(existing);

            ProductResponse response =
                    createResponse(id, "Updated Phone");

            when(mapper.mapToResponse(existing))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.updateProduct(
                            id,
                            request,
                            image,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .isSameAs(response);

            assertThat(existing.getImageUrl())
                    .isEqualTo(
                            "https://cloudinary.com/new-image.jpg"
                    );

            verify(cloudinaryService)
                    .extractPublicId(
                            "https://cloudinary.com/old-image.jpg"
                    );

            verify(cloudinaryService)
                    .deleteImage("old-image");

            verify(cloudinaryService)
                    .uploadProductImage(
                            image,
                            "11",
                            "ADMIN"
                    );

            verify(productPort)
                    .save(existing);
        }
    }


    @Nested
    @DisplayName("Basic Product Tests")
    class BasicProductTests {

        @Test
        @DisplayName("Should return product by ID")
        void getProductById_shouldReturnProduct() {

            // Arrange
            Long id = 20L;

            Product product =
                    createProduct(
                            id,
                            "Monitor",
                            new BigDecimal("300.00"),
                            10,
                            "Electronics",
                            true
                    );

            ProductResponse response =
                    createResponse(id, "Monitor");

            when(productPort.findById(id))
                    .thenReturn(Optional.of(product));

            when(mapper.mapToResponse(product))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.getProductById(id);

            // Assert
            assertThat(result)
                    .isSameAs(response);

            verify(productPort)
                    .findById(id);

            verify(mapper)
                    .mapToResponse(product);
        }

        @Test
        @DisplayName("Should throw exception when deleting missing product")
        void deleteProduct_shouldThrow_whenProductDoesNotExist() {

            // Arrange
            Long id = 999L;

            when(productPort.existsById(id))
                    .thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() ->
                    productService.deleteProduct(id)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(
                            "Product not found with id: 999"
                    );

            verify(productPort)
                    .existsById(id);

            verify(productPort, never())
                    .deleteById(id);
        }
    }

    @Nested
    @DisplayName("Stock Tests")
    class StockTests {

        @Test
        @DisplayName("Should update product stock")
        void updateProductStock_shouldUpdateQuantity() {

            // Arrange
            Long id = 30L;

            Product product =
                    createProduct(
                            id,
                            "Keyboard",
                            new BigDecimal("50.00"),
                            5,
                            "Accessories",
                            true
                    );

            ProductResponse response =
                    createResponse(id, "Keyboard");

            when(productPort.findById(id))
                    .thenReturn(Optional.of(product));

            when(productPort.save(product))
                    .thenReturn(product);

            when(mapper.mapToResponse(product))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.updateProductStock(
                            id,
                            25,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .isSameAs(response);

            assertThat(product.getStockQuantity())
                    .isEqualTo(25);

            assertThat(product.getUpdatedBy())
                    .isEqualTo("admin@test.com");

            verify(productPort)
                    .save(product);
        }

        @Test
        @DisplayName("Should return only active products at or below stock threshold")
        void findLowStockProducts_shouldFilterByStockAndActiveStatus() {

            // Arrange
            Product lowActive =
                    createProduct(
                            1L,
                            "Mouse",
                            new BigDecimal("20.00"),
                            3,
                            "Accessories",
                            true
                    );

            Product highActive =
                    createProduct(
                            2L,
                            "Keyboard",
                            new BigDecimal("40.00"),
                            20,
                            "Accessories",
                            true
                    );

            Product lowInactive =
                    createProduct(
                            3L,
                            "Old Mouse",
                            new BigDecimal("15.00"),
                            2,
                            "Accessories",
                            false
                    );

            ProductResponse lowActiveResponse =
                    createResponse(1L, "Mouse");

            when(productPort.findAll())
                    .thenReturn(
                            List.of(
                                    lowActive,
                                    highActive,
                                    lowInactive
                            )
                    );

            when(mapper.mapToResponse(lowActive))
                    .thenReturn(lowActiveResponse);

            // Act
            List<ProductResponse> result =
                    productService.findLowStockProducts(5);

            // Assert
            assertThat(result)
                    .containsExactly(lowActiveResponse);

            verify(mapper)
                    .mapToResponse(lowActive);

            verify(mapper, never())
                    .mapToResponse(highActive);

            verify(mapper, never())
                    .mapToResponse(lowInactive);
        }

        @Test
        @DisplayName("Should continue bulk update when one product fails")
        void bulkUpdateStock_shouldContinueWhenOneUpdateFails() {

            // Arrange
            Map<Long, Integer> updates =
                    Map.of(
                            1L, 10,
                            2L, 20
                    );

            ProductResponse successfulResponse =
                    createResponse(1L, "Mouse");

            ProductServiceImpl spyService =
                    spy(productService);

            doReturn(successfulResponse)
                    .when(spyService)
                    .updateProductStock(
                            1L,
                            10,
                            "admin@test.com"
                    );

            doThrow(
                    new RuntimeException("Product not found")
            )
                    .when(spyService)
                    .updateProductStock(
                            2L,
                            20,
                            "admin@test.com"
                    );

            // Act
            List<ProductResponse> result =
                    spyService.bulkUpdateStock(
                            updates,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .containsExactly(successfulResponse);

            verify(spyService)
                    .updateProductStock(
                            1L,
                            10,
                            "admin@test.com"
                    );

            verify(spyService)
                    .updateProductStock(
                            2L,
                            20,
                            "admin@test.com"
                    );
        }
    }

    @Nested
    @DisplayName("Analysis Tests")
    class AnalysisTests {

        @Test
        @DisplayName("Should calculate product statistics")
        void getProductStatistics_shouldCalculateStatistics() {

            // Arrange
            Product p1 =
                    createProduct(
                            1L,
                            "Laptop",
                            new BigDecimal("1000.00"),
                            5,
                            "Electronics",
                            true
                    );

            Product p2 =
                    createProduct(
                            2L,
                            "Phone",
                            new BigDecimal("500.00"),
                            4,
                            "Electronics",
                            false
                    );

            when(productPort.findAll())
                    .thenReturn(List.of(p1, p2));

            when(productPort.findByIsActive(true))
                    .thenReturn(List.of(p1));

            // Act
            Map<String, Object> result =
                    productService.getProductStatistics();

            // Assert
            assertThat(result.get("totalProducts"))
                    .isEqualTo(2L);

            assertThat(result.get("activeProducts"))
                    .isEqualTo(1L);

            assertThat(result.get("inactive"))
                    .isEqualTo(1L);

            assertThat(result.get("totalInventoryValue"))
                    .isEqualTo(7000.0);

            assertThat(result.get("totalStock"))
                    .isEqualTo(9);
        }

        @Test
        @DisplayName("Should calculate category price analysis")
        void getCategoryPriceAnalysis_shouldCalculateStatistics() {

            // Arrange
            String category = "Electronics";

            Product p1 =
                    createProduct(
                            1L,
                            "Phone",
                            new BigDecimal("500.00"),
                            5,
                            category,
                            true
                    );

            Product p2 =
                    createProduct(
                            2L,
                            "Laptop",
                            new BigDecimal("1500.00"),
                            2,
                            category,
                            true
                    );

            when(productPort.findByCategory(category))
                    .thenReturn(List.of(p1, p2));

            // Act
            Map<String, Object> result =
                    productService.getCategoryPriceAnalysis(category);

            // Assert
            assertThat(result.get("category"))
                    .isEqualTo(category);

            assertThat(result.get("productCount"))
                    .isEqualTo(2);

            assertThat(result.get("averagePrice"))
                    .isEqualTo(1000.0);

            assertThat(result.get("minPrice"))
                    .isEqualTo(500.0);

            assertThat(result.get("maxPrice"))
                    .isEqualTo(1500.0);

            assertThat(result.get("totalValue"))
                    .isEqualTo(2000.0);
        }
    }

    @Nested
    @DisplayName("Product State Tests")
    class ProductStateTests {

        @Test
        @DisplayName("Should toggle active product to inactive")
        void toggleProductActive_shouldToggleState() {

            // Arrange
            Long id = 40L;

            Product product =
                    createProduct(
                            id,
                            "Camera",
                            new BigDecimal("800.00"),
                            8,
                            "Electronics",
                            true
                    );

            ProductResponse response =
                    createResponse(id, "Camera");

            when(productPort.findById(id))
                    .thenReturn(Optional.of(product));

            when(productPort.save(product))
                    .thenReturn(product);

            when(mapper.mapToResponse(product))
                    .thenReturn(response);

            // Act
            ProductResponse result =
                    productService.toggleProductActive(
                            id,
                            "admin@test.com"
                    );

            // Assert
            assertThat(result)
                    .isSameAs(response);

            assertThat(product.getIsActive())
                    .isFalse();

            assertThat(product.getUpdatedBy())
                    .isEqualTo("admin@test.com");

            verify(productPort)
                    .save(product);
        }
    }


    @Nested
    @DisplayName("Search and Category Tests")
    class SearchAndCategoryTests {

        @Test
        @DisplayName("Should search active products by name ignoring case")
        void searchProductByName_shouldReturnMatchingActiveProducts() {

            // Arrange
            Product matching =
                    createProduct(
                            1L,
                            "Gaming Laptop",
                            new BigDecimal("1200.00"),
                            10,
                            "Electronics",
                            true
                    );

            Product inactiveMatching =
                    createProduct(
                            2L,
                            "Gaming Laptop Stand",
                            new BigDecimal("100.00"),
                            10,
                            "Accessories",
                            false
                    );

            Product nonMatching =
                    createProduct(
                            3L,
                            "Office Chair",
                            new BigDecimal("200.00"),
                            5,
                            "Furniture",
                            true
                    );

            ProductResponse response =
                    createResponse(1L, "Gaming Laptop");

            when(productPort.findAll())
                    .thenReturn(
                            List.of(
                                    matching,
                                    inactiveMatching,
                                    nonMatching
                            )
                    );

            when(mapper.mapToResponse(matching))
                    .thenReturn(response);

            // Act
            List<ProductResponse> result =
                    productService.searchProductByName("LAPTOP");

            // Assert
            assertThat(result)
                    .containsExactly(response);

            verify(mapper)
                    .mapToResponse(matching);

            verify(mapper, never())
                    .mapToResponse(inactiveMatching);

            verify(mapper, never())
                    .mapToResponse(nonMatching);
        }

        @Test
        @DisplayName("Should return distinct sorted categories")
        void getAllCategories_shouldReturnDistinctSortedCategories() {

            // Arrange
            Product p1 =
                    createProduct(
                            1L,
                            "Laptop",
                            new BigDecimal("1000.00"),
                            5,
                            "Electronics",
                            true
                    );

            Product p2 =
                    createProduct(
                            2L,
                            "Phone",
                            new BigDecimal("500.00"),
                            5,
                            "Accessories",
                            true
                    );

            Product p3 =
                    createProduct(
                            3L,
                            "Tablet",
                            new BigDecimal("700.00"),
                            5,
                            "Electronics",
                            true
                    );

            Product p4 =
                    createProduct(
                            4L,
                            "Unknown",
                            new BigDecimal("100.00"),
                            5,
                            null,
                            true
                    );

            when(productPort.findAll())
                    .thenReturn(
                            List.of(p1, p2, p3, p4)
                    );

            // Act
            List<String> result =
                    productService.getAllCategories();

            // Assert
            assertThat(result)
                    .containsExactly(
                            "Accessories",
                            "Electronics"
                    );
        }
    }


    @Nested
    @DisplayName("Price Tests")
    class PriceTests {

        @Test
        @DisplayName("Should sort active products by descending price")
        void getProductSortedByPrice_shouldSortDescending() {

            // Arrange
            Product cheap =
                    createProduct(
                            1L,
                            "Cheap",
                            new BigDecimal("100.00"),
                            5,
                            "Test",
                            true
                    );

            Product expensive =
                    createProduct(
                            2L,
                            "Expensive",
                            new BigDecimal("500.00"),
                            5,
                            "Test",
                            true
                    );

            Product middle =
                    createProduct(
                            3L,
                            "Middle",
                            new BigDecimal("300.00"),
                            5,
                            "Test",
                            true
                    );

            ProductResponse expensiveResponse =
                    createResponse(2L, "Expensive");

            ProductResponse middleResponse =
                    createResponse(3L, "Middle");

            ProductResponse cheapResponse =
                    createResponse(1L, "Cheap");

            when(productPort.findAll())
                    .thenReturn(
                            List.of(
                                    cheap,
                                    expensive,
                                    middle
                            )
                    );

            when(mapper.mapToResponse(expensive))
                    .thenReturn(expensiveResponse);

            when(mapper.mapToResponse(middle))
                    .thenReturn(middleResponse);

            when(mapper.mapToResponse(cheap))
                    .thenReturn(cheapResponse);

            // Act
            List<ProductResponse> result =
                    productService.getProductSortedByPrice("desc");

            // Assert
            assertThat(result)
                    .containsExactly(
                            expensiveResponse,
                            middleResponse,
                            cheapResponse
                    );
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("Should return existing active products for comparison")
        void compareProducts_shouldIgnoreMissingAndInactiveProducts() {

            // Arrange
            Product active =
                    createProduct(
                            1L,
                            "Laptop",
                            new BigDecimal("1000.00"),
                            10,
                            "Electronics",
                            true
                    );

            Product inactive =
                    createProduct(
                            2L,
                            "Old Laptop",
                            new BigDecimal("800.00"),
                            5,
                            "Electronics",
                            false
                    );

            ProductResponse activeResponse =
                    createResponse(1L, "Laptop");

            when(productPort.findById(1L))
                    .thenReturn(Optional.of(active));

            when(productPort.findById(2L))
                    .thenReturn(Optional.of(inactive));

            when(productPort.findById(3L))
                    .thenReturn(Optional.empty());

            when(mapper.mapToResponse(active))
                    .thenReturn(activeResponse);

            // Act
            List<ProductResponse> result =
                    productService.compareProducts(
                            List.of(1L, 2L, 3L)
                    );

            // Assert
            assertThat(result)
                    .containsExactly(activeResponse);

            verify(mapper)
                    .mapToResponse(active);

            verify(mapper, never())
                    .mapToResponse(inactive);
        }
    }

    private ProductRequest createProductRequest(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String category,
            Boolean active) {

        return ProductRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stock)
                .category(category)
                .isActive(active)
                .build();
    }


    private Product createProduct(
            Long id,
            String name,
            BigDecimal price,
            Integer stock,
            String category,
            Boolean active) {

        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setDescription("Test description");
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setIsActive(active);
        product.setImageUrl(null);
        product.setCreatedBy("admin@test.com");
        product.setUpdatedBy(null);

        return product;
    }


    private ProductResponse createResponse(
            Long id,
            String name) {

        return ProductResponse.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .category("Test")
                .isActive(true)
                .build();
    }
}