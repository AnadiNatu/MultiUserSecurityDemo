package com.example.MultiUserSecurityDemo.adapter.security.oauth2;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType2Entity;
import com.example.MultiUserSecurityDemo.adapter.security.security_files.JwtUtil;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType2Details;
import com.example.MultiUserSecurityDemo.adapter.web.service.impl.OAuth2ServiceImpl;
import com.example.MultiUserSecurityDemo.domain.model.UserRoles2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2SuccessHandler Tests")
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OAuth2ServiceImpl oAuth2Service;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2SuccessHandler oauth2SuccessHandler;


    private static final String REDIRECT_URI =
            "http://localhost:5173/oauth2/callback";


    @BeforeEach
    void setUp() {

        /*
         * oauthRedirectUri is injected through @Value in the
         * production application.
         *
         * Because this is a pure Mockito test, Spring is not
         * processing @Value, so we inject it manually.
         */
        ReflectionTestUtils.setField(
                oauth2SuccessHandler,
                "oauthRedirectUri",
                REDIRECT_URI);
    }

    @Test
    @DisplayName("Should successfully process OAuth2 login using sub as provider ID")
    void onAuthenticationSuccess_shouldRedirectWithTokens_whenOAuthLoginSucceeds()
            throws Exception {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);

        when(oAuth2User.getAttribute("email"))
                .thenReturn("john@gmail.com");

        when(oAuth2User.getAttribute("name"))
                .thenReturn("John Doe");

        when(oAuth2User.getAttribute("sub"))
                .thenReturn("google-123");

        when(oAuth2Service.determineProvider(oAuth2User))
                .thenReturn("google");

        UserType2Entity entity =
                createUserEntity(
                        10L,
                        "john@gmail.com",
                        "John",
                        "Doe",
                        "USER_TYPE2");

        when(oAuth2Service.handleOAuthUser(
                eq("john@gmail.com"),
                eq("John Doe"),
                eq("google"),
                eq("google-123"),
                eq(oAuth2User)))
                .thenReturn(entity);

        when(jwtUtil.generateToken(any(UserType2Details.class)))
                .thenReturn("access-token");

        when(jwtUtil.generateRefreshToken("john@gmail.com"))
                .thenReturn("refresh-token");

        // Act
        oauth2SuccessHandler.onAuthenticationSuccess(
                request,
                response,
                authentication);

        // Assert
        assertThat(response.getRedirectedUrl())
                .isNotNull();

        assertThat(response.getRedirectedUrl())
                .startsWith(REDIRECT_URI);

        assertThat(response.getRedirectedUrl())
                .contains("token=access-token");

        assertThat(response.getRedirectedUrl())
                .contains("refreshToken=refresh-token");

        assertThat(response.getRedirectedUrl())
                .contains("email=john%40gmail.com");

        assertThat(response.getRedirectedUrl())
                .contains("username=John+Doe");

        assertThat(response.getRedirectedUrl())
                .contains("userType=TYPE2");

        assertThat(response.getRedirectedUrl())
                .contains("role=USER_TYPE2");

        assertThat(response.getRedirectedUrl())
                .contains("provider=google");

        // Verify OAuth service interaction
        verify(oAuth2Service)
                .determineProvider(oAuth2User);

        verify(oAuth2Service)
                .handleOAuthUser(
                        eq("john@gmail.com"),
                        eq("John Doe"),
                        eq("google"),
                        eq("google-123"),
                        eq(oAuth2User));

        // Verify token generation
        verify(jwtUtil)
                .generateToken(any(UserType2Details.class));

        verify(jwtUtil)
                .generateRefreshToken("john@gmail.com");

        // Handler explicitly marks OAuth users approved/verified.
        assertThat(entity.isApproved())
                .isTrue();

        assertThat(entity.isEmailVerified())
                .isTrue();
    }

    @Test
    @DisplayName("Should use id attribute when OAuth2 sub attribute is unavailable")
    void onAuthenticationSuccess_shouldUseIdFallback_whenSubIsMissing()
            throws Exception {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);

        when(oAuth2User.getAttribute("email"))
                .thenReturn("github@test.com");

        when(oAuth2User.getAttribute("name"))
                .thenReturn("GitHub User");

        when(oAuth2User.getAttribute("sub"))
                .thenReturn(null);

        when(oAuth2User.getAttribute("id"))
                .thenReturn(123456789);

        when(oAuth2Service.determineProvider(oAuth2User))
                .thenReturn("github");

        UserType2Entity entity =
                createUserEntity(
                        20L,
                        "github@test.com",
                        "GitHub",
                        "User",
                        "USER_TYPE2");

        when(oAuth2Service.handleOAuthUser(
                eq("github@test.com"),
                eq("GitHub User"),
                eq("github"),
                eq("123456789"),
                eq(oAuth2User)))
                .thenReturn(entity);

        when(jwtUtil.generateToken(any(UserType2Details.class)))
                .thenReturn("github-token");

        when(jwtUtil.generateRefreshToken("github@test.com"))
                .thenReturn("github-refresh");

        // Act
        oauth2SuccessHandler.onAuthenticationSuccess(
                request,
                response,
                authentication);

        // Assert
        assertThat(response.getRedirectedUrl())
                .contains("token=github-token");

        assertThat(response.getRedirectedUrl())
                .contains("refreshToken=github-refresh");

        assertThat(response.getRedirectedUrl())
                .contains("provider=github");

        // Most important assertion:
        verify(oAuth2Service)
                .handleOAuthUser(
                        eq("github@test.com"),
                        eq("GitHub User"),
                        eq("github"),
                        eq("123456789"),
                        eq(oAuth2User));
    }

    @Test
    @DisplayName("Should redirect with error when OAuth2 provider does not provide email")
    void onAuthenticationSuccess_shouldRedirectError_whenEmailMissing()
            throws Exception {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);

        when(oAuth2User.getAttribute("email"))
                .thenReturn(null);

        when(oAuth2User.getAttribute("name"))
                .thenReturn("No Email User");

        when(oAuth2Service.determineProvider(oAuth2User))
                .thenReturn("github");

        // Act
        oauth2SuccessHandler.onAuthenticationSuccess(
                request,
                response,
                authentication);

        // Assert
        assertThat(response.getRedirectedUrl())
                .isNotNull();

        assertThat(response.getRedirectedUrl())
                .startsWith(REDIRECT_URI + "?error=");

        assertThat(response.getRedirectedUrl())
                .contains("Email+not+provided+by+github");

        // OAuth user must not be created/updated.
        verify(oAuth2Service, never())
                .handleOAuthUser(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any());

        verify(jwtUtil, never())
                .generateToken(any(UserType2Details.class));

        verify(jwtUtil, never())
                .generateRefreshToken(anyString());
    }

    @Test
    @DisplayName("Should redirect with error when OAuth2 user processing fails")
    void onAuthenticationSuccess_shouldRedirectError_whenOAuth2ServiceFails()
            throws Exception {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);

        when(oAuth2User.getAttribute("email"))
                .thenReturn("failure@test.com");

        when(oAuth2User.getAttribute("name"))
                .thenReturn("Failure User");

        when(oAuth2User.getAttribute("sub"))
                .thenReturn("provider-999");

        when(oAuth2Service.determineProvider(oAuth2User))
                .thenReturn("google");

        when(oAuth2Service.handleOAuthUser(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(OAuth2User.class)))
                .thenThrow(
                        new RuntimeException(
                                "Database unavailable"));

        // Act
        oauth2SuccessHandler.onAuthenticationSuccess(
                request,
                response,
                authentication);

        // Assert
        assertThat(response.getRedirectedUrl())
                .isNotNull();

        assertThat(response.getRedirectedUrl())
                .startsWith(REDIRECT_URI + "?error=");

        assertThat(response.getRedirectedUrl())
                .contains("Email+not+provided+by+google");

        verify(jwtUtil, never())
                .generateToken(any(UserType2Details.class));

        verify(jwtUtil, never())
                .generateRefreshToken(anyString());
    }

    @Test
    @DisplayName("Should redirect with error when JWT generation fails")
    void onAuthenticationSuccess_shouldRedirectError_whenTokenGenerationFails()
            throws Exception {

        // Arrange
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);

        when(oAuth2User.getAttribute("email"))
                .thenReturn("tokenfail@test.com");

        when(oAuth2User.getAttribute("name"))
                .thenReturn("Token Failure");

        when(oAuth2User.getAttribute("sub"))
                .thenReturn("google-token-failure");

        when(oAuth2Service.determineProvider(oAuth2User))
                .thenReturn("google");

        UserType2Entity entity =
                createUserEntity(
                        30L,
                        "tokenfail@test.com",
                        "Token",
                        "Failure",
                        "USER_TYPE2");

        when(oAuth2Service.handleOAuthUser(
                eq("tokenfail@test.com"),
                eq("Token Failure"),
                eq("google"),
                eq("google-token-failure"),
                eq(oAuth2User)))
                .thenReturn(entity);

        when(jwtUtil.generateToken(any(UserType2Details.class)))
                .thenThrow(
                        new RuntimeException(
                                "JWT generation failed"));

        // Act
        oauth2SuccessHandler.onAuthenticationSuccess(
                request,
                response,
                authentication);

        // Assert
        assertThat(response.getRedirectedUrl())
                .isNotNull();

        assertThat(response.getRedirectedUrl())
                .startsWith(REDIRECT_URI + "?error=");

        assertThat(response.getRedirectedUrl())
                .contains("Email+not+provided+by+google");

        verify(oAuth2Service)
                .handleOAuthUser(
                        eq("tokenfail@test.com"),
                        eq("Token Failure"),
                        eq("google"),
                        eq("google-token-failure"),
                        eq(oAuth2User));

        verify(jwtUtil)
                .generateToken(any(UserType2Details.class));

        verify(jwtUtil, never())
                .generateRefreshToken(anyString());
    }


    private UserType2Entity createUserEntity(
            Long id,
            String email,
            String fname,
            String lname,
            String role) {

        UserType2Entity entity =
                new UserType2Entity();

        entity.setId(id);
        entity.setEmail(email);
        entity.setFname(fname);
        entity.setLname(lname);
        entity.setPassword("encoded-password");
        entity.setPhoneNumber("9876543210");
        entity.setRole(role);
        entity.setApproved(false);
        entity.setEmailVerified(false);

        return entity;
    }


    @AfterEach
    void tearDown() {
        // No Spring Security context to clean here,
        // but keeping the test lifecycle clean.
    }
}
