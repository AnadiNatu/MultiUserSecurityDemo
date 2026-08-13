package com.example.MultiUserSecurityDemo.adapter.web.service;

import com.example.MultiUserSecurityDemo.adapter.web.dto.SignUpRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.SignUpResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.impl.AuthServiceImpl;
import com.example.MultiUserSecurityDemo.domain.model.UserRoles1;
import com.example.MultiUserSecurityDemo.domain.model.UserRoles2;
import com.example.MultiUserSecurityDemo.domain.model.UserType1;
import com.example.MultiUserSecurityDemo.domain.model.UserType2;
import com.example.MultiUserSecurityDemo.domain.port.UserType1Port;
import com.example.MultiUserSecurityDemo.domain.port.UserType2Port;
import com.example.MultiUserSecurityDemo.notification.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserType1Port userType1Port;

    @Mock
    private UserType2Port userType2Port;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should reject signup when email already exists")
    void signup_shouldReturnAlreadyExists_whenEmailExists() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "existing@test.com",
                "TYPE1"
        );

        when(userType1Port.findByEmail("existing@test.com"))
                .thenReturn(Optional.of(new UserType1()));

        // Act
        SignUpResponse response =
                authService.signup(request);

        // Assert
        assertThat(response.getId())
                .isNull();

        assertThat(response.getMessage())
                .isEqualTo("Email already exists");

        verify(userType1Port)
                .findByEmail("existing@test.com");

        verify(userType2Port, never())
                .findByEmail(anyString());

        verify(userType1Port, never())
                .save(any(UserType1.class));

        verify(userType2Port, never())
                .save(any(UserType2.class));

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should successfully register TYPE1 user and send verification OTP")
    void signup_shouldRegisterType1AndSendOtp() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "type1@test.com",
                "TYPE1"
        );

        request.setRole("ADMIN_TYPE1");

        UserType1 savedUser = createType1User(
                101L,
                "type1@test.com",
                "John",
                "Doe",
                UserRoles1.ADMIN_TYPE1
        );

        when(userType1Port.findByEmail("type1@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("type1@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userType1Port.save(any(UserType1.class)))
                .thenReturn(savedUser);

        // Act
        SignUpResponse response =
                authService.signup(request);

        // Assert
        assertThat(response.getId())
                .isEqualTo(101L);

        assertThat(response.getEmail())
                .isEqualTo("type1@test.com");

        assertThat(response.getUserType())
                .isEqualTo("TYPE1");

        assertThat(response.getRole())
                .isEqualTo("ADMIN_TYPE1");

        assertThat(response.getMessage())
                .contains("Registration successful");

        verify(passwordEncoder)
                .encode("Password123");

        verify(userType1Port)
                .save(any(UserType1.class));

        verify(emailService)
                .sendOtpViaEmail("type1@test.com");

        verify(userType2Port, never())
                .save(any(UserType2.class));
    }

    @Test
    @DisplayName("Should successfully register TYPE2 user and send verification OTP")
    void signup_shouldRegisterType2AndSendOtp() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "type2@test.com",
                "TYPE2"
        );

        request.setRole("USER_TYPE2");

        UserType2 savedUser = createType2User(
                202L,
                "type2@test.com",
                "Jane",
                "Smith",
                UserRoles2.USER_TYPE2
        );

        when(userType1Port.findByEmail("type2@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("type2@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userType2Port.save(any(UserType2.class)))
                .thenReturn(savedUser);

        // Act
        SignUpResponse response =
                authService.signup(request);

        // Assert
        assertThat(response.getId())
                .isEqualTo(202L);

        assertThat(response.getEmail())
                .isEqualTo("type2@test.com");

        assertThat(response.getUserType())
                .isEqualTo("TYPE2");

        assertThat(response.getRole())
                .isEqualTo("USER_TYPE2");

        assertThat(response.getMessage())
                .contains("Registration successful");

        verify(passwordEncoder)
                .encode("Password123");

        verify(userType2Port)
                .save(any(UserType2.class));

        verify(emailService)
                .sendOtpViaEmail("type2@test.com");

        verify(userType1Port, never())
                .save(any(UserType1.class));
    }

    @Test
    @DisplayName("Should reject signup when user type is invalid")
    void signup_shouldRejectInvalidUserType() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "invalid@test.com",
                "TYPE3"
        );

        when(userType1Port.findByEmail("invalid@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("invalid@test.com"))
                .thenReturn(Optional.empty());

        // Act
        SignUpResponse response =
                authService.signup(request);

        // Assert
        assertThat(response.getId())
                .isNull();

        assertThat(response.getMessage())
                .isEqualTo(
                        "Invalid user type. Use TYPE1 or TYPE2"
                );

        verify(userType1Port, never())
                .save(any(UserType1.class));

        verify(userType2Port, never())
                .save(any(UserType2.class));

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should keep signup successful when verification OTP sending fails")
    void signup_shouldRemainSuccessful_whenEmailOtpFails() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "otp-failure@test.com",
                "TYPE1"
        );

        UserType1 savedUser = createType1User(
                303L,
                "otp-failure@test.com",
                "OTP",
                "Failure",
                UserRoles1.ADMIN_TYPE1
        );

        when(userType1Port.findByEmail("otp-failure@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("otp-failure@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userType1Port.save(any(UserType1.class)))
                .thenReturn(savedUser);

        doThrow(new RuntimeException("Mail server unavailable"))
                .when(emailService)
                .sendOtpViaEmail("otp-failure@test.com");

        // Act
        SignUpResponse response =
                authService.signup(request);

        // Assert
        assertThat(response.getId())
                .isEqualTo(303L);

        assertThat(response.getMessage())
                .contains("Registration successful");

        verify(userType1Port)
                .save(any(UserType1.class));

        verify(emailService)
                .sendOtpViaEmail("otp-failure@test.com");
    }

    @Test
    @DisplayName("Should return false when email does not exist in either user type")
    void emailExists_shouldReturnFalse_whenEmailDoesNotExist() {

        // Arrange
        String email = "missing@test.com";

        when(userType1Port.findByEmail(email))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act
        boolean result =
                authService.emailExists(email);

        // Assert
        assertThat(result)
                .isFalse();

        verify(userType1Port)
                .findByEmail(email);

        verify(userType2Port)
                .findByEmail(email);
    }

    @Test
    @DisplayName("Should create TYPE1 user with encoded password and default verification flags")
    void registerUserType1_shouldCreateCorrectUser() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "type1-register@test.com",
                "TYPE1"
        );

        request.setRole("ADMIN_TYPE1");

        UserType1 savedUser = createType1User(
                404L,
                "type1-register@test.com",
                "Alice",
                "Brown",
                UserRoles1.ADMIN_TYPE1
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userType1Port.save(any(UserType1.class)))
                .thenReturn(savedUser);

        // Act
        SignUpResponse response =
                authService.registerUserType1(request);

        // Assert
        assertThat(response.getId())
                .isEqualTo(404L);

        assertThat(response.getUserType())
                .isEqualTo("TYPE1");

        ArgumentCaptor<UserType1> captor =
                ArgumentCaptor.forClass(UserType1.class);

        verify(userType1Port)
                .save(captor.capture());

        UserType1 saved =
                captor.getValue();

        assertThat(saved.getFname())
                .isEqualTo("Alice");

        assertThat(saved.getLname())
                .isEqualTo("Brown");

        assertThat(saved.getEmail())
                .isEqualTo("type1-register@test.com");

        assertThat(saved.getPassword())
                .isEqualTo("encoded-password");

        assertThat(saved.getPhoneNumber())
                .isEqualTo("9876543210");

        assertThat(saved.getRoles1())
                .isEqualTo(UserRoles1.ADMIN_TYPE1);

        assertThat(saved.isApproved())
                .isFalse();

        assertThat(saved.isEmailVerified())
                .isFalse();

        verify(passwordEncoder)
                .encode("Password123");
    }

    @Test
    @DisplayName("Should return registration failure when TYPE1 persistence fails")
    void registerUserType1_shouldReturnFailure_whenSaveFails() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "type1-failure@test.com",
                "TYPE1"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userType1Port.save(any(UserType1.class)))
                .thenThrow(
                        new RuntimeException("Database error"));

        // Act
        SignUpResponse response =
                authService.registerUserType1(request);

        // Assert
        assertThat(response.getId())
                .isNull();

        assertThat(response.getMessage())
                .isEqualTo(
                        "Registration failed: Database error"
                );

        verify(userType1Port)
                .save(any(UserType1.class));

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should create TYPE2 user with correct role and verification state")
    void signup_shouldCreateCorrectType2User() {

        // Arrange
        SignUpRequest request = createSignupRequest(
                "type2-details@test.com",
                "TYPE2"
        );

        request.setRole("USER_TYPE2");

        UserType2 savedUser = createType2User(
                505L,
                "type2-details@test.com",
                "Bob",
                "Wilson",
                UserRoles2.USER_TYPE2
        );

        when(userType1Port.findByEmail("type2-details@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("type2-details@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userType2Port.save(any(UserType2.class)))
                .thenReturn(savedUser);

        // Act
        SignUpResponse response =
                authService.signup(request);

        // Assert
        assertThat(response.getId())
                .isEqualTo(505L);

        ArgumentCaptor<UserType2> captor =
                ArgumentCaptor.forClass(UserType2.class);

        verify(userType2Port)
                .save(captor.capture());

        UserType2 saved =
                captor.getValue();

        assertThat(saved.getEmail())
                .isEqualTo("type2-details@test.com");

        assertThat(saved.getPassword())
                .isEqualTo("encoded-password");

        assertThat(saved.getRole())
                .isEqualTo(UserRoles2.USER_TYPE2);

        assertThat(saved.isApproved())
                .isFalse();

        assertThat(saved.isEmailVerified())
                .isFalse();
    }

    private SignUpRequest createSignupRequest(
            String email,
            String userType) {

        SignUpRequest request =
                new SignUpRequest();

        request.setFname("Test");
        request.setLname("User");
        request.setEmail(email);
        request.setPassword("Password123");
        request.setPhoneNumber("9876543210");
        request.setUserType(userType);
        request.setRole("USER_TYPE2");
        request.setCreatedByAdmin(false);

        return request;
    }


    private UserType1 createType1User(
            Long id,
            String email,
            String fname,
            String lname,
            UserRoles1 role) {

        UserType1 user =
                new UserType1();

        user.setId(id);
        user.setEmail(email);
        user.setFname(fname);
        user.setLname(lname);
        user.setPassword("encoded-password");
        user.setPhoneNumber("9876543210");
        user.setRoles1(role);
        user.setApproved(false);
        user.setEmailVerified(false);

        return user;
    }


    private UserType2 createType2User(
            Long id,
            String email,
            String fname,
            String lname,
            UserRoles2 role) {

        UserType2 user =
                new UserType2();

        user.setId(id);
        user.setEmail(email);
        user.setFname(fname);
        user.setLname(lname);
        user.setPassword("encoded-password");
        user.setPhoneNumber("9876543210");
        user.setRole(role);
        user.setApproved(false);
        user.setEmailVerified(false);

        return user;
    }
}
