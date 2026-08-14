package com.example.MultiUserSecurityDemo.adapter.web.controller;

import com.example.MultiUserSecurityDemo.adapter.web.dto.product.ProductRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.product.ProductResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.ProductService;
import com.example.MultiUserSecurityDemo.common.BaseControllerTest;
import com.example.MultiUserSecurityDemo.common.TestDataFactory;
import com.example.MultiUserSecurityDemo.common.TestUsers;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest extends BaseControllerTest {

    @MockBean
    ProductService productService;

    @Nested
    class AdminCreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void adminCreateProduct_shouldReturnCreated() throws Exception {

            ProductRequest request = TestDataFactory.productRequest();

            ProductResponse response = TestDataFactory.productResponse();

            MockMultipartFile productPart =
                    new MockMultipartFile(
                            "product",
                            "",
                            MediaType.APPLICATION_JSON_VALUE,
                            objectMapper.writeValueAsBytes(request));

            MockMultipartFile image =
                    new MockMultipartFile(
                            "image",
                            "laptop.jpg",
                            MediaType.IMAGE_JPEG_VALUE,
                            "dummy-image".getBytes(StandardCharsets.UTF_8));

            when(productService.createProduct(
                    any(ProductRequest.class),
                    any(),
                    eq("admin@test.com")))
                    .thenReturn(response);

            mockMvc.perform(multipart("/api/product/admin/create")
                            .file(productPart)
                            .file(image)
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message")
                            .value("Product created by ADMIN"))
                    .andExpect(jsonPath("$.createdBy")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.role")
                            .value("ADMIN"))
                    .andExpect(jsonPath("$.product.id")
                            .value(response.getId()));

            verify(productService)
                    .createProduct(any(ProductRequest.class),
                            any(),
                            eq("admin@test.com"));
        }

        @Test
        @DisplayName("Should return 400 when creation fails")
        void adminCreateProduct_shouldReturn400() throws Exception {

            ProductRequest request = TestDataFactory.productRequest();

            MockMultipartFile productPart =
                    new MockMultipartFile(
                            "product",
                            "",
                            MediaType.APPLICATION_JSON_VALUE,
                            objectMapper.writeValueAsBytes(request));

            when(productService.createProduct(
                    any(ProductRequest.class),
                    any(),
                    anyString()))
                    .thenThrow(new RuntimeException("Image upload failed"));

            mockMvc.perform(multipart("/api/product/admin/create")
                            .file(productPart)
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error")
                            .value("Failed to create product"))
                    .andExpect(jsonPath("$.details")
                            .value("Image upload failed"));

            verify(productService)
                    .createProduct(any(),
                            any(),
                            anyString());
        }
    }

    @Nested
    class AdminViewAllProductsTests {
        @Test
        @DisplayName("Should return all products with statistics")
        void adminViewAllProducts_shouldReturnProducts() throws Exception {

            List<ProductResponse> products = List.of(
                    TestDataFactory.productResponse(),
                    TestDataFactory.productResponse()
            );

            Map<String,Object> statistics = new HashMap<>();
            statistics.put("totalProducts",2);
            statistics.put("activeProducts",2);
            statistics.put("inactiveProducts",0);

            when(productService.getAllProducts())
                    .thenReturn(products);

            when(productService.getProductStatistics())
                    .thenReturn(statistics);

            mockMvc.perform(get("/api/product/admin/all")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products.length()")
                            .value(2))
                    .andExpect(jsonPath("$.totalCount")
                            .value(2))
                    .andExpect(jsonPath("$.accessedBy")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.role")
                            .value("ADMIN"))
                    .andExpect(jsonPath("$.message")
                            .value("Full admin access to all products"))
                    .andExpect(jsonPath("$.statistics.totalProducts")
                            .value(2));

            verify(productService)
                    .getAllProducts();

            verify(productService)
                    .getProductStatistics();
        }
    }

    @Nested
    class AdminDeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void adminDeleteProduct_shouldReturn200() throws Exception {

            doNothing().when(productService)
                    .deleteProduct(1L);

            mockMvc.perform(delete("/api/product/admin/delete/1")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Product permanently deleted by ADMIN"))
                    .andExpect(jsonPath("$.productId")
                            .value(1))
                    .andExpect(jsonPath("$.deletedBy")
                            .value("admin@test.com"));

            verify(productService)
                    .deleteProduct(1L);
        }

        @Test
        @DisplayName("Should return 404 when product does not exist")
        void adminDeleteProduct_shouldReturn404() throws Exception {

            doThrow(new RuntimeException("Product not found"))
                    .when(productService)
                    .deleteProduct(999L);

            mockMvc.perform(delete("/api/product/admin/delete/999")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error")
                            .value("Product not found"))
                    .andExpect(jsonPath("$.details")
                            .value("Product not found"));

            verify(productService)
                    .deleteProduct(999L);
        }
    }

    @Nested
    @DisplayName("PUT /api/product/admin-type1/update-stock/{id}")
    class UpdateStockTests {

        @Test
        @DisplayName("Should update stock successfully")
        void updateStock_shouldReturn200() throws Exception {

            ProductResponse response = TestDataFactory.productResponse();
            response.setStockQuantity(50);

            when(productService.updateProductStock(1L, 50, "admin@test.com")).thenReturn(response);

            mockMvc.perform(put("/api/product/admin-type1/update-stock/1")
                            .with(user(TestUsers.admin()))
                            .param("quantity", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Stock updated successfully"))
                    .andExpect(jsonPath("$.updatedBy")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.product.stockQuantity")
                            .value(50));

            verify(productService).updateProductStock(1L, 50, "admin@test.com");
        }

        @Test
        @DisplayName("Should return 404 when product is missing")
        void updateStock_shouldReturn404() throws Exception {

            when(productService.updateProductStock(999L, 50, "admin@test.com")).thenThrow(new RuntimeException("Product not found"));

            mockMvc.perform(put("/api/product/admin-type1/update-stock/999")
                            .with(user(TestUsers.admin()))
                            .param("quantity", "50"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error")
                            .value("Product not found"))
                    .andExpect(jsonPath("$.details")
                            .value("Product not found"));

            verify(productService).updateProductStock(999L,50, "admin@test.com");
        }

    }

    @Nested
    @DisplayName("PUT /api/product/admin-type1/bulk-update-stock")
    class BulkUpdateStockTests {

        @Test
        @DisplayName("Should bulk update stock successfully")
        void bulkUpdateStock_shouldReturn200() throws Exception {

            Map<Long, Integer> updates = new HashMap<>();
            updates.put(1L, 20);
            updates.put(2L, 40);

            List<ProductResponse> updatedProducts = List.of(
                    TestDataFactory.productResponse(),
                    TestDataFactory.productResponse()
            );

            when(productService.bulkUpdateStock(
                    eq(updates),
                    eq("admin@test.com")))
                    .thenReturn(updatedProducts);

            mockMvc.perform(put("/api/product/admin-type1/bulk-update-stock")
                            .with(user(TestUsers.admin()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Bulk stock update completed"))
                    .andExpect(jsonPath("$.updatedCount")
                            .value(2))
                    .andExpect(jsonPath("$.updatedBy")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.products.length()")
                            .value(2));

            verify(productService)
                    .bulkUpdateStock(
                            eq(updates),
                            eq("admin@test.com"));
        }

        @Test
        @DisplayName("Should return empty result for empty update request")
        void bulkUpdateStock_shouldHandleEmptyRequest() throws Exception {

            Map<Long, Integer> updates = Collections.emptyMap();

            when(productService.bulkUpdateStock(
                    eq(updates),
                    eq("admin@test.com")))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(put("/api/product/admin-type1/bulk-update-stock")
                            .with(user(TestUsers.admin()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.updatedCount")
                            .value(0))
                    .andExpect(jsonPath("$.products.length()")
                            .value(0));

            verify(productService)
                    .bulkUpdateStock(
                            eq(updates),
                            eq("admin@test.com"));
        }
    }


    @Nested
    @DisplayName("GET /api/product/admin-type1/low-stock")
    class LowStockTests {

        @Test
        @DisplayName("Should return low stock products")
        void getLowStockProducts_shouldReturnProducts() throws Exception {

            List<ProductResponse> lowStockProducts = List.of(
                    TestDataFactory.productResponse(),
                    TestDataFactory.productResponse()
            );

            when(productService.findLowStockProducts(10))
                    .thenReturn(lowStockProducts);

            mockMvc.perform(get("/api/product/admin-type1/low-stock")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products.length()")
                            .value(2))
                    .andExpect(jsonPath("$.count")
                            .value(2))
                    .andExpect(jsonPath("$.threshold")
                            .value(10))
                    .andExpect(jsonPath("$.role")
                            .value("ADMIN_TYPE1"))
                    .andExpect(jsonPath("$.message")
                            .value("Low stock alert - Inventory management"));

            verify(productService)
                    .findLowStockProducts(10);
        }


        @Test
        @DisplayName("Should return empty list when no products are low in stock")
        void getLowStockProducts_shouldReturnEmptyList() throws Exception {

            when(productService.findLowStockProducts(10))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/product/admin-type1/low-stock")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count")
                            .value(0))
                    .andExpect(jsonPath("$.products.length()")
                            .value(0))
                    .andExpect(jsonPath("$.threshold")
                            .value(10));

            verify(productService)
                    .findLowStockProducts(10);
        }
    }

    @Nested
    @DisplayName("PUT /api/product/admin-type2/update-price/{id}")
    class UpdatePriceTests {

        @Test
        @DisplayName("Should update product successfully")
        void updatePrice_shouldReturn200() throws Exception {

            ProductRequest request = TestDataFactory.productRequest();
            request.setPrice(new BigDecimal("99999"));

            ProductResponse response =
                    TestDataFactory.productResponse();

            response.setPrice(new BigDecimal("99999"));

            when(productService.updateProduct(
                    eq(1L),
                    any(ProductRequest.class),
                    isNull(),
                    eq("admin2@test.com")
            )).thenReturn(response);

            MockMultipartFile productPart =
                    new MockMultipartFile(
                            "product",
                            "",
                            MediaType.APPLICATION_JSON_VALUE,
                            objectMapper.writeValueAsBytes(request)
                    );

            mockMvc.perform(multipart(
                            "/api/product/admin-type2/update-price/1")
                            .file(productPart)
                            .with(user(TestUsers.adminType2()))
                            .with(req -> {
                                req.setMethod("PUT");
                                return req;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Price updated by ADMIN_TYPE2"))
                    .andExpect(jsonPath("$.updatedBy")
                            .value("admin2@test.com"))
                    .andExpect(jsonPath("$.product.price")
                            .value(99999))
                    .andExpect(jsonPath("$.newPrice")
                            .value(99999));

            verify(productService).updateProduct(
                    eq(1L),
                    any(ProductRequest.class),
                    isNull(),
                    eq("admin2@test.com")
            );
        }
    }

        @Nested
        @DisplayName("PATCH /api/product/admin-type2/toggle-active/{id}")
        class ToggleActiveTests {

            @Test
            @DisplayName("Should activate product successfully")
            void toggleActive_shouldActivateProduct() throws Exception {

                ProductResponse response = TestDataFactory.productResponse();
                response.setIsActive(true);

                when(productService.toggleProductActive(1L, "admin2@test.com")).thenReturn(response);

                mockMvc.perform(patch("/api/product/admin-type2/toggle-active/1")
                                .with(user(TestUsers.adminType2())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message")
                                .value("Product active status updated"))
                        .andExpect(jsonPath("$.updatedBy")
                                .value("admin2@test.com"))
                        .andExpect(jsonPath("$.product.isActive")
                                .value(true));

                verify(productService).toggleProductActive(1L, "admin2@test.com");
            }

            @Test
            @DisplayName("Should deactivate product successfully")
            void toggleActive_shouldDeactivateProduct() throws Exception {

                ProductResponse response = TestDataFactory.productResponse();
                response.setIsActive(false);

                when(productService.toggleProductActive(1L, "admin2@test.com")).thenReturn(response);

                mockMvc.perform(patch("/api/product/admin-type2/toggle-active/1")
                                .with(user(TestUsers.adminType2())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.product.isActive")
                                .value(false));

                verify(productService).toggleProductActive(1L, "admin2@test.com");
            }

            @Test
            @DisplayName("Should return 404 when toggled product does not exist")
            void toggleActive_shouldReturn404() throws Exception {

                when(productService.toggleProductActive(999L, "admin2@test.com"))
                        .thenThrow(new RuntimeException("Product not found"));

                mockMvc.perform(patch("/api/product/admin-type2/toggle-active/999")
                                .with(user(TestUsers.adminType2())))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error")
                                .value("Product not found"))
                        .andExpect(jsonPath("$.details")
                                .value("Product not found"));

                verify(productService).toggleProductActive(999L, "admin2@test.com");
            }
        }

        @Nested
        @DisplayName("GET /api/product/user/details/{id}")
        class ProductDetailsTests {

            @Test
            @DisplayName("Should return active product details")
            void getProductDetails_shouldReturnProduct() throws Exception {

                ProductResponse response = TestDataFactory.productResponse();
                response.setIsActive(true);

                when(productService.getProductById(1L))
                        .thenReturn(response);

                mockMvc.perform(get("/api/product/user/details/1")
                                .with(user(TestUsers.user())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id")
                                .value(response.getId()))
                        .andExpect(jsonPath("$.name")
                                .value(response.getName()))
                        .andExpect(jsonPath("$.isActive")
                                .value(true));

                verify(productService)
                        .getProductById(1L);
            }

            @Test
            @DisplayName("Should return 404 for inactive product")
            void getProductDetails_shouldReturn404_whenInactive() throws Exception {

                ProductResponse response = TestDataFactory.productResponse();
                response.setIsActive(false);

                when(productService.getProductById(1L))
                        .thenReturn(response);

                mockMvc.perform(get("/api/product/user/details/1")
                                .with(user(TestUsers.user())))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error")
                                .value("Product not available"));

                verify(productService)
                        .getProductById(1L);
            }

            @Test
            @DisplayName("Should return 404 when product does not exist")
            void getProductDetails_shouldReturn404_whenMissing() throws Exception {

                when(productService.getProductById(999L))
                        .thenThrow(new RuntimeException("Product not found"));

                mockMvc.perform(get("/api/product/user/details/999")
                                .with(user(TestUsers.user())))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error")
                                .value("Product not found"));

                verify(productService)
                        .getProductById(999L);
            }
        }

        @Nested
        @DisplayName("GET /api/product/user/search")
        class SearchProductTests {
            @Test
            @DisplayName("Should return matching products")
            void searchProducts_shouldReturnResults() throws Exception {

                List<ProductResponse> products = List.of(
                        TestDataFactory.productResponse(),
                        TestDataFactory.productResponse()
                );

                when(productService.getProductsByCategory("Laptop"))
                        .thenReturn(products);

                mockMvc.perform(get("/api/product/user/search")
                                .with(user(TestUsers.user()))
                                .param("keyword", "Laptop"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()")
                                .value(2));

                verify(productService).getProductsByCategory("Laptop");
            }

            @Test
            @DisplayName("Should return empty list when no products match")
            void searchProducts_shouldReturnEmptyList() throws Exception {

                when(productService.searchProductByName("Unknown"))
                        .thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/product/user/search")
                                .with(user(TestUsers.user()))
                                .param("keyword", "Unknown"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()")
                                .value(0));

                verify(productService)
                        .searchProductByName("Unknown");
            }
        }

        @Nested
        @DisplayName("POST /api/product/user-type2/compare")
        class CompareProductsTests {

            @Test
            @DisplayName("Should compare products successfully")
            void compareProducts_shouldReturnComparison() throws Exception {

                List<Long> ids = List.of(1L, 2L);

                List<ProductResponse> comparison = List.of(
                        TestDataFactory.productResponse(),
                        TestDataFactory.productResponse()
                );

                when(productService.compareProducts(ids))
                        .thenReturn(comparison);

                mockMvc.perform(post("/api/product/user-type2/compare")
                                .with(user(TestUsers.userType2()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ids)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()")
                                .value(2));

                verify(productService)
                        .compareProducts(ids);
            }

            @Test
            @DisplayName("Should return empty comparison")
            void compareProducts_shouldReturnEmptyList() throws Exception {

                List<Long> ids = List.of();

                when(productService.compareProducts(ids))
                        .thenReturn(Collections.emptyList());

                mockMvc.perform(post("/api/product/user-type2/compare")
                                .with(user(TestUsers.userType2()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ids)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()")
                                .value(0));

                verify(productService)
                        .compareProducts(ids);
            }
        }
    }
