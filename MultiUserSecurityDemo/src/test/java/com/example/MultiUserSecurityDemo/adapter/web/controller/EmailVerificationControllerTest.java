package com.example.MultiUserSecurityDemo.adapter.web.controller;

import com.example.MultiUserSecurityDemo.domain.model.UserType1;
import com.example.MultiUserSecurityDemo.domain.model.UserType2;
import com.example.MultiUserSecurityDemo.domain.port.UserType1Port;
import com.example.MultiUserSecurityDemo.domain.port.UserType2Port;
import com.example.MultiUserSecurityDemo.notification.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(EmailVerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmailVerificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EmailService emailService;

    @MockBean
    UserType1Port userType1Port;

    @MockBean
    UserType2Port userType2Port;

    @Nested
    @DisplayName("POST /api/auth/verify-email")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify TYPE1 user successfully")
        void verifyEmail_shouldVerifyType1User() throws Exception {

            String email = "admin@test.com";
            String otp = "123456";

            UserType1 user = new UserType1();
            user.setEmail(email);

            when(emailService.validateOtp(email, otp)).thenReturn(true);
            when(userType1Port.findByEmail(email)).thenReturn(Optional.of(user));

            mockMvc.perform(post("/api/auth/verify-email")
                            .param("email", email)
                            .param("otp", otp))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.verified").value(true))
                    .andExpect(jsonPath("$.userType").value("TYPE1"));

            verify(userType1Port).save(any(UserType1.class));
            verify(userType2Port, never()).save(any());
        }


        @Test
        @DisplayName("Should verify TYPE2 user successfully")
        void verifyEmail_shouldVerifyType2User() throws Exception {

            String email = "user@test.com";
            String otp = "123456";

            UserType2 user = new UserType2();
            user.setEmail(email);

            when(emailService.validateOtp(email, otp)).thenReturn(true);

            when(userType1Port.findByEmail(email))
                    .thenReturn(Optional.empty());

            when(userType2Port.findByEmail(email))
                    .thenReturn(Optional.of(user));

            mockMvc.perform(post("/api/auth/verify-email")
                            .param("email", email)
                            .param("otp", otp))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.verified").value(true))
                    .andExpect(jsonPath("$.userType").value("TYPE2"));

            verify(userType2Port).save(any(UserType2.class));
        }


        @Test
        @DisplayName("Should return bad request when OTP is invalid")
        void verifyEmail_shouldReturn400_whenOtpInvalid() throws Exception {

            String email = "admin@test.com";
            String otp = "000000";

            when(emailService.validateOtp(email, otp))
                    .thenReturn(false);

            mockMvc.perform(post("/api/auth/verify-email")
                            .param("email", email)
                            .param("otp", otp))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.verified").value(false))
                    .andExpect(jsonPath("$.message")
                            .value("Invalid or expired OTP"));

            verify(userType1Port, never()).save(any());
            verify(userType2Port, never()).save(any());
        }


        @Test
        @DisplayName("Should return bad request when user is not found")
        void verifyEmail_shouldReturn400_whenUserNotFound() throws Exception {

            String email = "nouser@test.com";
            String otp = "123456";

            when(emailService.validateOtp(email, otp))
                    .thenReturn(true);

            when(userType1Port.findByEmail(email))
                    .thenReturn(Optional.empty());

            when(userType2Port.findByEmail(email))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/api/auth/verify-email")
                            .param("email", email)
                            .param("otp", otp))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.verified").value(false))
                    .andExpect(jsonPath("$.message")
                            .value("User not found"));
        }

    }

    @Nested
    @DisplayName("POST /api/auth/resend-verification")
    class ResendVerificationTests {

        @Test
        @DisplayName("Should resend OTP for TYPE1 user")
        void resendVerification_shouldSendOtpForType1() throws Exception {

            String email = "admin@test.com";

            UserType1 user = new UserType1();

            when(userType1Port.findByEmail(email))
                    .thenReturn(Optional.of(user));

            doNothing().when(emailService)
                    .sendOtpViaEmail(email);

            mockMvc.perform(post("/api/auth/resend-verification")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Verification OTP sent. Please check your inbox."));

            verify(emailService).sendOtpViaEmail(email);
        }


        @Test
        @DisplayName("Should resend OTP for TYPE2 user")
        void resendVerification_shouldSendOtpForType2() throws Exception {

            String email = "user@test.com";

            UserType2 user = new UserType2();

            when(userType1Port.findByEmail(email))
                    .thenReturn(Optional.empty());

            when(userType2Port.findByEmail(email))
                    .thenReturn(Optional.of(user));

            doNothing().when(emailService)
                    .sendOtpViaEmail(email);

            mockMvc.perform(post("/api/auth/resend-verification")
                            .param("email", email))
                    .andExpect(status().isOk());

            verify(emailService).sendOtpViaEmail(email);
        }


        @Test
        @DisplayName("Should return generic message when user does not exist")
        void resendVerification_shouldReturnGenericMessage_whenUserDoesNotExist() throws Exception {

            String email = "nouser@test.com";

            when(userType1Port.findByEmail(email))
                    .thenReturn(Optional.empty());

            when(userType2Port.findByEmail(email))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/api/auth/resend-verification")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("If an account exists with this email, a verification code has been sent."));

            verify(emailService, never()).sendOtpViaEmail(any());
        }


        @Test
        @DisplayName("Should return 500 when email service throws exception")
        void resendVerification_shouldReturn500_whenEmailFails() throws Exception {

            String email = "admin@test.com";

            UserType1 user = new UserType1();

            when(userType1Port.findByEmail(email))
                    .thenReturn(Optional.of(user));

            doThrow(new RuntimeException("Email failed"))
                    .when(emailService)
                    .sendOtpViaEmail(email);

            mockMvc.perform(post("/api/auth/resend-verification")
                            .param("email", email))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value("Failed to send verification email. Please try again."));
        }
    }
}
