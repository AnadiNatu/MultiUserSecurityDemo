package com.example.MultiUserSecurityDemo.common;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.OrderEntity;
import com.example.MultiUserSecurityDemo.adapter.persistence.entity.ProductEntity;
import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType1Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType2Entity;
import com.example.MultiUserSecurityDemo.adapter.web.dto.order.OrderRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.order.OrderResponse;
import com.example.MultiUserSecurityDemo.adapter.web.dto.product.ProductRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.product.ProductResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TestDataFactory {

    private TestDataFactory(){}

    public static ProductRequest productRequest(){

        return ProductRequest.builder()
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("89999"))
                .stockQuantity(15)
                .category("Electronics")
                .imageUrl("image.jpg")
                .isActive(true)
                .build();
    }

    public static ProductResponse productResponse(){

        return ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("89999"))
                .stockQuantity(15)
                .category("Electronics")
                .imageUrl("image.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin@test.com")
                .updatedBy("admin@test.com")
                .build();
    }

    public static ProductEntity productEntity(){

        return ProductEntity.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("89999"))
                .stockQuantity(15)
                .category("Electronics")
                .imageUrl("image.jpg")
                .isActive(true)
                .ownerType("TYPE1")
                .createdBy("admin@test.com")
                .updatedBy("admin@test.com")
                .build();
    }

    public static OrderRequest orderRequest(){

        OrderRequest.OrderItemRequest item =
                OrderRequest.OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(2)
                        .build();

        return OrderRequest.builder()
                .items(List.of(item))
                .build();
    }

    public static OrderResponse orderResponse(){

        OrderResponse.OrderItemResponse item =
                OrderResponse.OrderItemResponse.builder()
                        .id("1")
                        .productId("1")
                        .productName("Laptop")
                        .quantity(2)
                        .price(new BigDecimal("89999"))
                        .subtotal(new BigDecimal("179998"))
                        .build();

        return OrderResponse.builder()
                .id("1")
                .userId("admin@test.com")
                .userName("Admin User")
                .items(List.of(item))
                .status("PENDING")
                .totalAmount(new BigDecimal("179998"))
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }

    public static OrderEntity orderEntity(){

        return OrderEntity.builder()
                .id(1L)
                .userEmail("admin@test.com")
                .userName("Admin User")
                .status("PENDING")
                .totalAmount(new BigDecimal("179998"))
                .build();
    }

    public static UserType1Entity type1Entity(){

        return UserType1Entity.builder()
                .id(1L)
                .fname("Admin")
                .lname("User")
                .email("admin@test.com")
                .password("password")
                .phoneNumber("9876543210")
                .role("ADMIN")
                .emailVerified(true)
                .isApproved(true)
                .build();
    }

    public static UserType2Entity type2Entity(){

        return UserType2Entity.builder()
                .id(2L)
                .fname("Normal")
                .lname("User")
                .email("user@test.com")
                .password("password")
                .phoneNumber("9876543211")
                .role("USER")
                .emailVerified(true)
                .isApproved(true)
                .build();
    }
}
