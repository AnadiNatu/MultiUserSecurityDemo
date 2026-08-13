package com.example.MultiUserSecurityDemo.adapter.security.security_files;

import com.example.MultiUserSecurityDemo.adapter.security.oauth2.OAuth2SuccessHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CompositeUserDetailService compositeUserDetailService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private OAuth2SuccessHandler oauth2SuccessHandler;

    @Test
    @DisplayName("Should allow unauthenticated access to auth endpoints")
    void security_shouldPermitAuthEndpointsWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/auth/login")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject protected request when user is not authenticated")
    void security_shouldRejectProtectedRequest_whenUnauthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/products")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow authenticated user to access protected request")
    void security_shouldAllowProtectedRequest_whenAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/products")
                                .with(user("user@test.com")
                                        .authorities(
                                                () -> "USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should allow ADMIN authority through authenticated security chain")
    void security_shouldAllowAdminUser()
            throws Exception {

        mockMvc.perform(
                        get("/api/orders/admin/all")
                                .with(user("admin@test.com")
                                        .authorities(
                                                () -> "ADMIN"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject normal user from admin-only endpoint")
    void security_shouldRejectNonAdminUserFromAdminEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/orders/admin/all")
                                .with(user("user@test.com")
                                        .authorities(
                                                () -> "USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should provide working BCrypt password encoder")
    void passwordEncoder_shouldEncodeAndMatchPassword() {

        String rawPassword = "TestPassword123!";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword)
                .isNotBlank();

        assertThat(encodedPassword)
                .isNotEqualTo(rawPassword);

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword))
                .isTrue();

        assertThat(
                passwordEncoder.matches(
                        "WrongPassword",
                        encodedPassword))
                .isFalse();
    }


}
