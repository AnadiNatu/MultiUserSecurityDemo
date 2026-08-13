package com.example.MultiUserSecurityDemo.adapter.web.controller;


import com.example.MultiUserSecurityDemo.notification.EmailService;
import com.example.MultiUserSecurityDemo.notification.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OtpController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OtpControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EmailService emailService;

    @MockBean
    SmsService smsService;

    @Nested
    @DisplayName("POST /api/otp/send/email")
    class SendEmailOtpTests {

        @Test
        @DisplayName("Should send email OTP successfully")
        void sendEmailOtp_shouldReturn200() throws Exception {

            // Arrange
            String email = "test@gmail.com";

            doNothing().when(emailService).sendOtpViaEmail(email);

            // Act + Assert
            mockMvc.perform(post("/api/otp/send/email")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("OTP sent to " + email));

            // Verify
            verify(emailService, times(1))
                    .sendOtpViaEmail(email);
        }

    }

    @Nested
    @DisplayName("POST /api/otp/verify/email")
    class VerifyEmailOtpTests {

        @Test
        @DisplayName("Should verify email OTP successfully")
        void verifyEmailOtp_shouldReturn200_whenOtpValid() throws Exception {

            String email = "test@gmail.com";
            String otp = "123456";

            when(emailService.validateOtp(email, otp))
                    .thenReturn(true);

            mockMvc.perform(post("/api/otp/verify/email")
                            .param("email", email)
                            .param("otp", otp))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.verified").value(true))
                    .andExpect(jsonPath("$.message")
                            .value("Email verified successfully"));

            verify(emailService)
                    .validateOtp(email, otp);
        }


        @Test
        @DisplayName("Should return 400 when email OTP is invalid")
        void verifyEmailOtp_shouldReturn400_whenOtpInvalid() throws Exception {

            String email = "test@gmail.com";
            String otp = "000000";

            when(emailService.validateOtp(email, otp))
                    .thenReturn(false);

            mockMvc.perform(post("/api/otp/verify/email")
                            .param("email", email)
                            .param("otp", otp))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.verified").value(false))
                    .andExpect(jsonPath("$.message")
                            .value("Invalid or expired OTP"));

            verify(emailService)
                    .validateOtp(email, otp);
        }

    }

    @Nested
    @DisplayName("POST /api/otp/send/sms")
    class SendSmsOtpTests {

        @Test
        @DisplayName("Should send SMS OTP successfully")
        void sendSmsOtp_shouldReturn200() throws Exception {

            String phone = "9876543210";

            doNothing().when(smsService)
                    .sendOtpViaSms(phone);

            mockMvc.perform(post("/api/otp/send/sms")
                            .param("phone", phone))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("OTP sent to " + phone));

            verify(smsService)
                    .sendOtpViaSms(phone);
        }

    }

    @Nested
    @DisplayName("POST /api/otp/verify/sms")
    class VerifySmsOtpTests {

        @Test
        @DisplayName("Should verify SMS OTP successfully")
        void verifySmsOtp_shouldReturn200_whenOtpValid() throws Exception {

            String phone = "9876543210";
            String otp = "654321";

            when(smsService.validateOtp(phone, otp))
                    .thenReturn(true);

            mockMvc.perform(post("/api/otp/verify/sms")
                            .param("phone", phone)
                            .param("otp", otp))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.verified").value(true))
                    .andExpect(jsonPath("$.message")
                            .value("Phone verified successfully"));

            verify(smsService)
                    .validateOtp(phone, otp);
        }


        @Test
        @DisplayName("Should return 400 when SMS OTP is invalid")
        void verifySmsOtp_shouldReturn400_whenOtpInvalid() throws Exception {

            String phone = "9876543210";
            String otp = "111111";

            when(smsService.validateOtp(phone, otp))
                    .thenReturn(false);

            mockMvc.perform(post("/api/otp/verify/sms")
                            .param("phone", phone)
                            .param("otp", otp))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.verified").value(false))
                    .andExpect(jsonPath("$.message")
                            .value("Invalid or expired OTP"));

            verify(smsService)
                    .validateOtp(phone, otp);
        }

    }
}
