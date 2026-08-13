package com.example.MultiUserSecurityDemo.adapter.web.controller;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType1Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType2Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.UserType1Repository;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.UserType2Repository;
import com.example.MultiUserSecurityDemo.adapter.security.security_files.JwtUtil;
import com.example.MultiUserSecurityDemo.notification.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhoneController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PhoneControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SmsService smsService;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserType1Repository userType1Repository;

    @MockBean
    UserType2Repository userType2Repository;

    @Nested
    class SendOtpTests{

        @Test
        @DisplayName("Should send OTP for TYPE1 user")
        void sendPhoneOtp_shouldReturn200_whenType1Exists() throws Exception {

            UserType1Entity user = new UserType1Entity();
            user.setPhoneNumber("9876543210");

            when(userType1Repository.findAll())
                    .thenReturn(List.of(user));

            when(userType2Repository.findAll())
                    .thenReturn(Collections.emptyList());

            doNothing().when(smsService)
                    .sendOtpViaSms("9876543210");

            mockMvc.perform(post("/api/auth/phone/send-otp")
                            .param("phone","9876543210"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("OTP sent to 9876543210"));

            verify(smsService).sendOtpViaSms("9876543210");
        }

        @Test
        @DisplayName("Should send OTP for TYPE2 user")
        void sendPhoneOtp_shouldReturn200_whenType2Exists() throws Exception {

            UserType2Entity user = new UserType2Entity();
            user.setPhoneNumber("9876543210");

            when(userType1Repository.findAll())
                    .thenReturn(Collections.emptyList());

            when(userType2Repository.findAll())
                    .thenReturn(List.of(user));

            doNothing().when(smsService)
                    .sendOtpViaSms("9876543210");

            mockMvc.perform(post("/api/auth/phone/send-otp")
                            .param("phone","9876543210"))
                    .andExpect(status().isOk());

            verify(smsService)
                    .sendOtpViaSms("9876543210");
        }

        @Test
        @DisplayName("Should return 500 when user not found")
        void sendPhoneOtp_shouldReturn500_whenUserNotFound() throws Exception {

            when(userType1Repository.findAll())
                    .thenReturn(Collections.emptyList());

            when(userType2Repository.findAll())
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(post("/api/auth/phone/send-otp")
                            .param("phone","9876543210"))
                    .andExpect(status().isInternalServerError());

            verify(smsService,never())
                    .sendOtpViaSms(any());
        }

    }


    @Nested
    @DisplayName("POST /api/auth/phone/verify-otp")
    class VerifyOtpTests{
        @Test
        @DisplayName("Should login TYPE1 successfully")
        void verifyPhoneOtp_shouldLoginType1() throws Exception {

            UserType1Entity entity = new UserType1Entity();

            entity.setId(1L);
            entity.setEmail("admin@test.com");
            entity.setPhoneNumber("9876543210");
            entity.setRole("ADMIN");

            when(smsService.validateOtp("9876543210","123456"))
                    .thenReturn(true);

            when(userType1Repository.findAll())
                    .thenReturn(List.of(entity));

            when(jwtUtil.generateToken(any()))
                    .thenReturn("jwt-token");

            mockMvc.perform(post("/api/auth/phone/verify-otp")
                            .param("phone","9876543210")
                            .param("otp","123456"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token")
                            .value("jwt-token"))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE1"));

            verify(jwtUtil).generateToken(any());
        }

        @Test
        @DisplayName("Should login TYPE2 successfully")
        void verifyPhoneOtp_shouldLoginType2() throws Exception {

            UserType2Entity entity = new UserType2Entity();

            entity.setId(1L);
            entity.setEmail("user@test.com");
            entity.setPhoneNumber("9876543210");
            entity.setRole("USER");

            when(smsService.validateOtp("9876543210","123456"))
                    .thenReturn(true);

            when(userType1Repository.findAll())
                    .thenReturn(Collections.emptyList());

            when(userType2Repository.findAll())
                    .thenReturn(List.of(entity));

            when(jwtUtil.generateToken(any()))
                    .thenReturn("jwt-token");

            mockMvc.perform(post("/api/auth/phone/verify-otp")
                            .param("phone","9876543210")
                            .param("otp","123456"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token")
                            .value("jwt-token"))
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE2"));
        }

        @Test
        @DisplayName("Should return bad request when OTP invalid")
        void verifyPhoneOtp_shouldReturn400_whenOtpInvalid() throws Exception {

            when(smsService.validateOtp("9876543210","000000"))
                    .thenReturn(false);

            mockMvc.perform(post("/api/auth/phone/verify-otp")
                            .param("phone","9876543210")
                            .param("otp","000000"))
                    .andExpect(status().isBadRequest());

            verify(jwtUtil,never())
                    .generateToken(any());
        }

        @Test
        @DisplayName("Should return not found when user does not exist")
        void verifyPhoneOtp_shouldReturn404_whenUserMissing() throws Exception {

            when(smsService.validateOtp("9876543210","123456"))
                    .thenReturn(true);

            when(userType1Repository.findAll())
                    .thenReturn(Collections.emptyList());

            when(userType2Repository.findAll())
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(post("/api/auth/phone/verify-otp")
                            .param("phone","9876543210")
                            .param("otp","123456"))
                    .andExpect(status().isNotFound());
        }
    }


}