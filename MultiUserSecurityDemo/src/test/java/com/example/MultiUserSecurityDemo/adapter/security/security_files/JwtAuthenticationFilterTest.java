package com.example.MultiUserSecurityDemo.adapter.security.security_files;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CompositeUserDetailService userDetailService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should continue filter chain when Authorization header is missing")
    void doFilter_shouldContinueChain_whenAuthorizationHeaderMissing()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        // Act
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userDetailService);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header is not Bearer")
    void doFilter_shouldContinueChain_whenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Basic abc123");

        // Act
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userDetailService);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    @DisplayName("Should authenticate user when JWT is valid")
    void doFilter_shouldAuthenticateUser_whenTokenIsValid()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer valid-jwt-token");

        UserDetails userDetails =
                User.withUsername("admin@test.com")
                        .password("password")
                        .authorities(
                                new SimpleGrantedAuthority("ADMIN"))
                        .build();

        when(jwtUtil.extractUsername("valid-jwt-token"))
                .thenReturn("admin@test.com");

        when(userDetailService.loadUserByUsername("admin@test.com"))
                .thenReturn(userDetails);

        when(jwtUtil.isTokenValid(
                "valid-jwt-token",
                userDetails))
                .thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getPrincipal())
                .isEqualTo(userDetails);

        assertThat(authentication.getAuthorities())
                .containsExactly(
                );

        assertThat(authentication.isAuthenticated())
                .isTrue();

        verify(jwtUtil)
                .extractUsername("valid-jwt-token");

        verify(userDetailService)
                .loadUserByUsername("admin@test.com");

        verify(jwtUtil)
                .isTokenValid(
                        "valid-jwt-token",
                        userDetails);

        verify(filterChain)
                .doFilter(request, response);
    }


    @Test
    @DisplayName("Should not authenticate user when JWT is invalid")
    void doFilter_shouldNotAuthenticateUser_whenTokenIsInvalid()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer invalid-jwt-token");

        UserDetails userDetails =
                User.withUsername("user@test.com")
                        .password("password")
                        .authorities(
                                new SimpleGrantedAuthority("USER"))
                        .build();

        when(jwtUtil.extractUsername("invalid-jwt-token"))
                .thenReturn("user@test.com");

        when(userDetailService.loadUserByUsername("user@test.com"))
                .thenReturn(userDetails);

        when(jwtUtil.isTokenValid(
                "invalid-jwt-token",
                userDetails))
                .thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtUtil)
                .extractUsername("invalid-jwt-token");

        verify(userDetailService)
                .loadUserByUsername("user@test.com");

        verify(jwtUtil)
                .isTokenValid(
                        "invalid-jwt-token",
                        userDetails);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    @DisplayName("Should not replace existing authentication")
    void doFilter_shouldNotReplaceAuthentication_whenContextAlreadyAuthenticated()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer valid-jwt-token");

        Authentication existingAuthentication =
                mock(Authentication.class);

        SecurityContextHolder
                .getContext()
                .setAuthentication(existingAuthentication);

        when(jwtUtil.extractUsername("valid-jwt-token"))
                .thenReturn("admin@test.com");

        // Act
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .isSameAs(existingAuthentication);

        verify(jwtUtil)
                .extractUsername("valid-jwt-token");

        verifyNoInteractions(userDetailService);

        verify(jwtUtil, never())
                .isTokenValid(
                        anyString(),
                        any(UserDetails.class));

        verify(filterChain)
                .doFilter(request, response);
    }


    @Test
    @DisplayName("Should clear security context when JWT processing throws exception")
    void doFilter_shouldClearContext_whenJwtProcessingFails()
            throws ServletException, IOException {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer broken-jwt-token");

        Authentication existingAuthentication =
                mock(Authentication.class);

        SecurityContextHolder
                .getContext()
                .setAuthentication(existingAuthentication);

        when(jwtUtil.extractUsername("broken-jwt-token"))
                .thenThrow(
                        new RuntimeException("Invalid JWT"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain);

        // Assert
        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .isNull();

        verify(jwtUtil)
                .extractUsername("broken-jwt-token");

        verifyNoInteractions(userDetailService);

        verify(filterChain)
                .doFilter(request, response);
    }




    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
