package com.example.MultiUserSecurityDemo.adapter.web.controller;


import com.example.MultiUserSecurityDemo.adapter.security.security_files.JwtUtil;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType1Details;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType2Details;
import com.example.MultiUserSecurityDemo.adapter.web.dto.SignUpRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.SignUpResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.AuthService;
import com.example.MultiUserSecurityDemo.common.BaseControllerTest;
import com.example.MultiUserSecurityDemo.common.TestUsers;
import com.example.MultiUserSecurityDemo.notification.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest extends BaseControllerTest {


    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    AuthService authService;

    @MockBean
    NotificationService notificationService;

    @Nested
    class SignupTests{

        @Test
        @DisplayName("Should return 400 when email is missing")
        void signup_shouldReturn400_whenEmailMissing() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setPassword("password");
            request.setUserType("TYPE1");

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Email is required"));

            verify(authService, never()).signup(any());

            verify(notificationService, never())
                    .sendWelcomeNotification(any(), any(), any());
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void signup_shouldReturn400_whenPasswordMissing() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setEmail("admin@test.com");
            request.setUserType("TYPE1");

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Password is required"));

            verify(authService, never()).signup(any());

        }

        @Test
        @DisplayName("Should return 400 when userType is missing")
        void signup_shouldReturn400_whenUserTypeMissing() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setEmail("admin@test.com");
            request.setPassword("password");

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("User Type is Required (TYPE1 or TYPE2)"));

            verify(authService, never()).signup(any());

        }

        @Test
        @DisplayName("Should register TYPE1 successfully")
        void signup_shouldRegisterType1() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setFname("Admin");
            request.setLname("User");
            request.setEmail("admin@test.com");
            request.setPassword("password");
            request.setPhoneNumber("9876543210");
            request.setUserType("TYPE1");

            SignUpResponse response = SignUpResponse.builder()
                    .id(1L)
                    .email("admin@test.com")
                    .fname("Admin")
                    .lname("User")
                    .userType("TYPE1")
                    .role("ADMIN")
                    .message("Registration successful")
                    .build();

            when(authService.signup(any(SignUpRequest.class)))
                    .thenReturn(response);

            doNothing().when(notificationService)
                    .sendWelcomeNotification(
                            anyString(),
                            anyString(),
                            anyString());

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id")
                            .value(1))
                    .andExpect(jsonPath("$.email")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE1"));

            verify(authService)
                    .signup(any(SignUpRequest.class));

            verify(notificationService)
                    .sendWelcomeNotification(
                            eq("admin@test.com"),
                            eq("9876543210"),
                            eq("Admin"));

        }

        @Test
        @DisplayName("Should register TYPE2 successfully")
        void signup_shouldRegisterType2() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setFname("John");
            request.setLname("Smith");
            request.setEmail("user@test.com");
            request.setPassword("password");
            request.setPhoneNumber("9999999999");
            request.setUserType("TYPE2");

            SignUpResponse response = SignUpResponse.builder()
                    .id(2L)
                    .email("user@test.com")
                    .fname("John")
                    .lname("Smith")
                    .userType("TYPE2")
                    .role("USER_TYPE2")
                    .message("Registration successful")
                    .build();

            when(authService.signup(any(SignUpRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id")
                            .value(2))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE2"));

            verify(authService)
                    .signup(any(SignUpRequest.class));

        }

        @Test
        @DisplayName("Should return 400 when signup fails")
        void signup_shouldReturn400_whenSignupFails() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setEmail("admin@test.com");
            request.setPassword("password");
            request.setUserType("TYPE1");

            SignUpResponse response = SignUpResponse.builder()
                    .message("Email already exists")
                    .build();

            when(authService.signup(any()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Email already exists"));

            verify(notificationService, never())
                    .sendWelcomeNotification(any(), any(), any());

        }

        @Test
        @DisplayName("Should still create account when notification fails")
        void signup_shouldIgnoreNotificationFailure() throws Exception {

            SignUpRequest request = new SignUpRequest();

            request.setFname("Admin");
            request.setEmail("admin@test.com");
            request.setPassword("password");
            request.setPhoneNumber("9876543210");
            request.setUserType("TYPE1");

            SignUpResponse response = SignUpResponse.builder()
                    .id(1L)
                    .email("admin@test.com")
                    .userType("TYPE1")
                    .build();

            when(authService.signup(any()))
                    .thenReturn(response);

            doThrow(new RuntimeException("SMS Gateway Down"))
                    .when(notificationService)
                    .sendWelcomeNotification(any(), any(), any());

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(notificationService)
                    .sendWelcomeNotification(any(), any(), any());

        }

    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests{

        @Test
        @DisplayName("Should login TYPE1 user successfully")
        void login_shouldReturnType1Response() throws Exception {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "admin@test.com");
            credentials.put("password", "password");

            UserType1Details user = TestUsers.admin();

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenReturn(authentication);

            when(jwtUtil.generateToken(user))
                    .thenReturn("jwt-token");

            when(jwtUtil.generateRefreshToken("admin@test.com"))
                    .thenReturn("refresh-token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token")
                            .value("jwt-token"))
                    .andExpect(jsonPath("$.refreshToken")
                            .value("refresh-token"))
                    .andExpect(jsonPath("$.email")
                            .value("admin@test.com"))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE1"))
                    .andExpect(jsonPath("$.message")
                            .value("Login successful"));

            verify(authenticationManager)
                    .authenticate(any(Authentication.class));

        }


        @Test
        @DisplayName("Should login TYPE2 user successfully")
        void login_shouldReturnType2Response() throws Exception {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "admin2@test.com");
            credentials.put("password", "password");

            UserType2Details user = TestUsers.adminType2();

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities());

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenReturn(authentication);

            when(jwtUtil.generateToken(user))
                    .thenReturn("jwt-token");

            when(jwtUtil.generateRefreshToken("admin2@test.com"))
                    .thenReturn("refresh-token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE2"))
                    .andExpect(jsonPath("$.email")
                            .value("admin2@test.com"))
                    .andExpect(jsonPath("$.token")
                            .value("jwt-token"));

        }

        @Test
        @DisplayName("Should return fallback response for generic UserDetails")
        void login_shouldReturnFallbackResponse() throws Exception {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "user@test.com");
            credentials.put("password", "password");

            UserDetails genericUser =
                    org.springframework.security.core.userdetails.User
                            .withUsername("user@test.com")
                            .password("password")
                            .authorities("USER")
                            .build();

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            genericUser,
                            null,
                            genericUser.getAuthorities());

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenReturn(authentication);

            when(jwtUtil.generateToken(genericUser))
                    .thenReturn("jwt-token");

            when(jwtUtil.generateRefreshToken("user@test.com"))
                    .thenReturn("refresh-token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username")
                            .value("user@test.com"))
                    .andExpect(jsonPath("$.role")
                            .value("USER"));

        }

        @Test
        @DisplayName("Should return 403 when account is disabled")
        void login_shouldReturn403_whenAccountDisabled() throws Exception {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "admin@test.com");
            credentials.put("password", "password");

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenThrow(new DisabledException("Disabled"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error")
                            .value("Account not yet active"))
                    .andExpect(jsonPath("$.hint")
                            .exists());

        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void login_shouldReturn401_whenBadCredentials() throws Exception {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "admin@test.com");
            credentials.put("password", "wrong");

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenThrow(new BadCredentialsException("Bad Credentials"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error")
                            .value("Invalid credentials"));

        }

        @Test
        @DisplayName("Should return 500 when unexpected exception occurs")
        void login_shouldReturn500_whenUnexpectedException() throws Exception {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "admin@test.com");
            credentials.put("password", "password");

            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenThrow(new RuntimeException("Database unavailable"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(credentials)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error")
                            .value("Login failed"))
                    .andExpect(jsonPath("$.message")
                            .value("Database unavailable"));

        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class MeEndpointTests{

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void me_shouldReturn401_whenUnauthenticated() throws Exception {

            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error")
                            .value("Not authenticated"));

        }

        @Test
        @DisplayName("Should return TYPE1 profile")
        void me_shouldReturnType1Profile() throws Exception {

            UserType1Details user = TestUsers.admin();

            mockMvc.perform(get("/api/auth/me")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(user.getUser().getId()))
                    .andExpect(jsonPath("$.email")
                            .value(user.getUser().getEmail()))
                    .andExpect(jsonPath("$.fname")
                            .value(user.getUser().getFname()))
                    .andExpect(jsonPath("$.lname")
                            .value(user.getUser().getLname()))
                    .andExpect(jsonPath("$.role")
                            .value(user.getUser().getRoles1().name()))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE1"))
                    .andExpect(jsonPath("$.phoneNumber")
                            .value(user.getUser().getPhoneNumber()));

        }

        @Test
        @DisplayName("Should return TYPE2 profile")
        void me_shouldReturnType2Profile() throws Exception {

            UserType2Details user = TestUsers.adminType2();

            mockMvc.perform(get("/api/auth/me")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(user.getUser().getId()))
                    .andExpect(jsonPath("$.email")
                            .value(user.getUser().getEmail()))
                    .andExpect(jsonPath("$.fname")
                            .value(user.getUser().getFname()))
                    .andExpect(jsonPath("$.lname")
                            .value(user.getUser().getLname()))
                    .andExpect(jsonPath("$.role")
                            .value(user.getUser().getRole().name()))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE2"))
                    .andExpect(jsonPath("$.phoneNumber")
                            .value(user.getUser().getPhoneNumber()));

        }

        @Test
        @DisplayName("Should return username for generic UserDetails")
        void me_shouldReturnGenericUser() throws Exception {

            UserDetails genericUser =
                    org.springframework.security.core.userdetails.User
                            .withUsername("generic@test.com")
                            .password("password")
                            .authorities("USER")
                            .build();

            mockMvc.perform(get("/api/auth/me")
                            .with(user(genericUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username")
                            .value("generic@test.com"));

        }
    }
}
