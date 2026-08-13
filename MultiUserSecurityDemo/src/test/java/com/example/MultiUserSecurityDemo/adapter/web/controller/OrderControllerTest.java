package com.example.MultiUserSecurityDemo.adapter.web.controller;


import com.example.MultiUserSecurityDemo.adapter.web.dto.order.OrderRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.order.OrderResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.OrderService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest extends BaseControllerTest {

    @MockBean
    private OrderService orderService;

        @Nested
        @DisplayName("POST /api/orders/create")
        class CreateOrderTests {

            @Test
            @DisplayName("Should create order successfully")
            void createOrder_shouldReturnCreated() throws Exception {

                // Arrange
                OrderRequest request = TestDataFactory.orderRequest();

                OrderResponse response = TestDataFactory.orderResponse();

                when(orderService.createOrder(
                        any(OrderRequest.class),
                        eq("admin@test.com"),
                        eq("Admin User")))
                        .thenReturn(response);

                // Act + Assert
                mockMvc.perform(post("/api/orders/create")
                                .with(user(TestUsers.admin()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.message")
                                .value("Order created successfully"))
                        .andExpect(jsonPath("$.order.id")
                                .value(response.getId()))
                        .andExpect(jsonPath("$.order.status")
                                .value(response.getStatus()));

                verify(orderService, times(1))
                        .createOrder(any(OrderRequest.class),
                                eq("admin@test.com"),
                                eq("Admin User"));
            }

            @Test
            @DisplayName("Should return 400 when service throws exception")
            void createOrder_shouldReturn400_whenServiceThrows() throws Exception {

                OrderRequest request = TestDataFactory.orderRequest();

                when(orderService.createOrder(
                        any(OrderRequest.class),
                        anyString(),
                        anyString()))
                        .thenThrow(new RuntimeException("Product out of stock"));

                mockMvc.perform(post("/api/orders/create")
                                .with(user(TestUsers.admin()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error")
                                .value("Failed to create order"))
                        .andExpect(jsonPath("$.details")
                                .value("Product out of stock"));

                verify(orderService)
                        .createOrder(any(OrderRequest.class),
                                anyString(),
                                anyString());
            }
        }


    @Nested
    @DisplayName("GET /api/orders/my")
    class GetMyOrdersTests {

        @Test
        @DisplayName("Should return logged-in user's orders")
        void getMyOrders_shouldReturnOrders() throws Exception {

            List<OrderResponse> orders = List.of(
                    TestDataFactory.orderResponse(),
                    TestDataFactory.orderResponse()
            );

            when(orderService.getOrdersByUser("admin@test.com"))
                    .thenReturn(orders);

            mockMvc.perform(get("/api/orders/my")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount")
                            .value(2))
                    .andExpect(jsonPath("$.userEmail")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.orders.length()")
                            .value(2));

            verify(orderService)
                    .getOrdersByUser("admin@test.com");
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void getMyOrders_shouldReturnEmptyList() throws Exception {

            when(orderService.getOrdersByUser("admin@test.com"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/orders/my")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount")
                            .value(0))
                    .andExpect(jsonPath("$.orders.length()")
                            .value(0))
                    .andExpect(jsonPath("$.userEmail")
                            .value("admin@test.com"));

            verify(orderService)
                    .getOrdersByUser("admin@test.com");
        }

    }

    @Nested
    @DisplayName("GET /api/orders/admin/all")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return all orders for admin")
        void getAllOrders_shouldReturnAllOrders() throws Exception {

            // Arrange
            List<OrderResponse> orders = List.of(
                    TestDataFactory.orderResponse(),
                    TestDataFactory.orderResponse(),
                    TestDataFactory.orderResponse()
            );

            when(orderService.getAllOrders())
                    .thenReturn(orders);

            // Act + Assert
            mockMvc.perform(get("/api/orders/admin/all")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount")
                            .value(3))
                    .andExpect(jsonPath("$.orders.length()")
                            .value(3))
                    .andExpect(jsonPath("$.accessedBy")
                            .value("admin@test.com"));

            verify(orderService)
                    .getAllOrders();
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderByIdTests {
        @Test
        @DisplayName("Admin should view any order")
        void admin_shouldViewAnyOrder() throws Exception {

            OrderResponse response = TestDataFactory.orderResponse();

            response.setUserId("someone@test.com");

            when(orderService.getOrderById(1L))
                    .thenReturn(response);

            mockMvc.perform(get("/api/orders/1")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(response.getId()))
                    .andExpect(jsonPath("$.userId")
                            .value("someone@test.com"));

            verify(orderService)
                    .getOrderById(1L);
        }

        @Test
        @DisplayName("Owner should view own order")
        void owner_shouldViewOwnOrder() throws Exception {

            OrderResponse response = TestDataFactory.orderResponse();

            response.setUserId("user@test.com");

            when(orderService.getOrderById(1L))
                    .thenReturn(response);

            mockMvc.perform(get("/api/orders/1")
                            .with(user(TestUsers.user())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId")
                            .value("user@test.com"));

            verify(orderService)
                    .getOrderById(1L);
        }

        @Test
        @DisplayName("User should receive 403 for another user's order")
        void user_shouldReturn403_whenViewingAnotherUsersOrder() throws Exception {

            OrderResponse response = TestDataFactory.orderResponse();

            response.setUserId("another@test.com");

            when(orderService.getOrderById(1L))
                    .thenReturn(response);

            mockMvc.perform(get("/api/orders/1")
                            .with(user(TestUsers.user())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error")
                            .value("Access denied"));

            verify(orderService)
                    .getOrderById(1L);
        }

        @Test
        @DisplayName("Should return 404 when order is missing")
        void shouldReturn404_whenOrderNotFound() throws Exception {

            when(orderService.getOrderById(999L))
                    .thenThrow(new RuntimeException("Order not found"));

            mockMvc.perform(get("/api/orders/999")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error")
                            .value("Order not found"))
                    .andExpect(jsonPath("$.details")
                            .value("Order not found"));

            verify(orderService)
                    .getOrderById(999L);
        }

    }


    @Nested
    @DisplayName("PUT /api/orders/admin/status/{id}")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status successfully")
        void updateStatus_shouldReturn200() throws Exception {

            OrderResponse updated = TestDataFactory.orderResponse();

            updated.setStatus("SHIPPED");

            when(orderService.updateOrderStatus(
                    1L,
                    "SHIPPED",
                    "admin@test.com"))
                    .thenReturn(updated);

            mockMvc.perform(put("/api/orders/admin/status/1")
                            .with(user(TestUsers.admin()))
                            .param("status", "SHIPPED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Order status updated"))
                    .andExpect(jsonPath("$.updatedBy")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.order.status")
                            .value("SHIPPED"));

            verify(orderService)
                    .updateOrderStatus(
                            1L,
                            "SHIPPED",
                            "admin@test.com");
        }

        @Test
        @DisplayName("Should return 400 when status is invalid")
        void updateStatus_shouldReturn400_whenStatusInvalid() throws Exception {

            when(orderService.updateOrderStatus(
                    1L,
                    "INVALID",
                    "admin@test.com"))
                    .thenThrow(new IllegalArgumentException("Invalid order status"));

            mockMvc.perform(put("/api/orders/admin/status/1")
                            .with(user(TestUsers.admin()))
                            .param("status", "INVALID"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error")
                            .value("Invalid status value"))
                    .andExpect(jsonPath("$.details")
                            .value("Invalid order status"));

            verify(orderService)
                    .updateOrderStatus(
                            1L,
                            "INVALID",
                            "admin@test.com");
        }

        @Test
        @DisplayName("Should return 404 when order does not exist")
        void updateStatus_shouldReturn404_whenOrderMissing() throws Exception {

            when(orderService.updateOrderStatus(
                    999L,
                    "DELIVERED",
                    "admin@test.com"))
                    .thenThrow(new RuntimeException("Order not found"));

            mockMvc.perform(put("/api/orders/admin/status/999")
                            .with(user(TestUsers.admin()))
                            .param("status", "DELIVERED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error")
                            .value("Order not found"))
                    .andExpect(jsonPath("$.details")
                            .value("Order not found"));

            verify(orderService)
                    .updateOrderStatus(
                            999L,
                            "DELIVERED",
                            "admin@test.com");
        }

    }

    @Nested
    @DisplayName("PUT /api/orders/cancel/{id}")
    class CancelOrderTests {
        @Test
        @DisplayName("Should cancel order successfully")
        void cancelOrder_shouldReturn200() throws Exception {

            OrderResponse cancelled = TestDataFactory.orderResponse();

            cancelled.setStatus("CANCELLED");

            when(orderService.cancelOrder(
                    1L,
                    "admin@test.com"))
                    .thenReturn(cancelled);

            mockMvc.perform(put("/api/orders/cancel/1")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Order cancelled successfully"))
                    .andExpect(jsonPath("$.order.status")
                            .value("CANCELLED"));

            verify(orderService)
                    .cancelOrder(
                            1L,
                            "admin@test.com");
        }

        @Test
        @DisplayName("Should propagate exception when cancellation fails")
        void cancelOrder_shouldThrowException_whenServiceFails() throws Exception {

            when(orderService.cancelOrder(
                    99L,
                    "admin@test.com"))
                    .thenThrow(new RuntimeException("Order cannot be cancelled"));

            mockMvc.perform(put("/api/orders/cancel/99")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isInternalServerError());

            verify(orderService)
                    .cancelOrder(
                            99L,
                            "admin@test.com");
        }
    }


    @Nested
    @DisplayName("DELETE Order Endpoints")
    class DeleteOrderTests {
        @Test
        @DisplayName("Should delete logged-in user's own order")
        void deleteOwnOrder_shouldReturn200() throws Exception {

            doNothing().when(orderService)
                    .deleteOwnOrder(1L, "admin@test.com");

            mockMvc.perform(delete("/api/orders/my/1").with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Order deleted successfully"));

            verify(orderService).deleteOwnOrder(1L, "admin@test.com");
        }

        @Test
        @DisplayName("Admin should delete any order")
        void deleteOrderAsAdmin_shouldReturn200() throws Exception {

            doNothing().when(orderService)
                    .deleteOrder(1L);

            mockMvc.perform(delete("/api/orders/admin/1")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Order deleted successfully"));

            verify(orderService)
                    .deleteOrder(1L);
        }

        @Test
        @DisplayName("Should return exception when deleting own order fails")
        void deleteOwnOrder_shouldThrowException() throws Exception {

            doThrow(new RuntimeException("Order not found"))
                    .when(orderService)
                    .deleteOwnOrder(
                            999L,
                            "admin@test.com");

            mockMvc.perform(delete("/api/orders/my/999")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isInternalServerError());

            verify(orderService)
                    .deleteOwnOrder(
                            999L,
                            "admin@test.com");
        }

        @Test
        @DisplayName("Should return exception when admin delete fails")
        void deleteOrderAsAdmin_shouldThrowException() throws Exception {

            doThrow(new RuntimeException("Order not found"))
                    .when(orderService)
                    .deleteOrder(999L);

            mockMvc.perform(delete("/api/orders/admin/999")
                            .with(user(TestUsers.admin())))
                    .andExpect(status().isInternalServerError());

            verify(orderService)
                    .deleteOrder(999L);
        }
    }

    @Nested
    @DisplayName("Search Order Endpoints")
    class SearchOrderTests {
        @Test
        @DisplayName("Should return orders by status")
        void getOrdersByStatus_shouldReturnOrders() throws Exception {

            List<OrderResponse> orders = List.of(
                    TestDataFactory.orderResponse(),
                    TestDataFactory.orderResponse()
            );

            when(orderService.getOrdersByStatus("PENDING"))
                    .thenReturn(orders);

            mockMvc.perform(get("/api/orders/admin/status")
                            .with(user(TestUsers.admin()))
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()")
                            .value(2));

            verify(orderService)
                    .getOrdersByStatus("PENDING");
        }

        @Test
        @DisplayName("Should return empty list for unknown status")
        void getOrdersByStatus_shouldReturnEmptyList() throws Exception {

            when(orderService.getOrdersByStatus("UNKNOWN"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/orders/admin/status")
                            .with(user(TestUsers.admin()))
                            .param("status", "UNKNOWN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()")
                            .value(0));

            verify(orderService)
                    .getOrdersByStatus("UNKNOWN");
        }

        @Test
        @DisplayName("Should return orders by user email")
        void getOrdersByUserEmail_shouldReturnOrders() throws Exception {

            List<OrderResponse> orders = List.of(
                    TestDataFactory.orderResponse()
            );

            when(orderService.getOrdersByUserEmail("admin@test.com"))
                    .thenReturn(orders);

            mockMvc.perform(get("/api/orders/admin/user")
                            .with(user(TestUsers.admin()))
                            .param("email", "admin@test.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()")
                            .value(1));

            verify(orderService)
                    .getOrdersByUserEmail("admin@test.com");
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void getOrdersByUserEmail_shouldReturnEmptyList() throws Exception {

            when(orderService.getOrdersByUserEmail("nouser@test.com"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/orders/admin/user")
                            .with(user(TestUsers.admin()))
                            .param("email", "nouser@test.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()")
                            .value(0));

            verify(orderService)
                    .getOrdersByUserEmail("nouser@test.com");
        }
    }

}