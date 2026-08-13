package com.example.MultiUserSecurityDemo.adapter.web.controller;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType1Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType2Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.UserType1Repository;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.UserType2Repository;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType1Details;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType2Details;
import com.example.MultiUserSecurityDemo.adapter.web.service.impl.CloudinaryService;
import com.example.MultiUserSecurityDemo.common.TestUsers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.Optional;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CloudinaryService cloudinaryService;

    @MockBean
    UserType1Repository userType1Repository;

    @MockBean
    UserType2Repository userType2Repository;

    @Nested
    class UploadProfilePhotoTests{
        @Test
        @DisplayName("Should upload profile photo for TYPE1")
        void uploadProfilePhoto_shouldUploadForType1() throws Exception {

            UserType1Entity entity = new UserType1Entity();
            entity.setId(1L);
            entity.setEmail("admin@test.com");

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "profile.jpg",
                            "image/jpeg",
                            "dummy image".getBytes());

            when(userType1Repository.findByEmail("admin@test.com"))
                    .thenReturn(Optional.of(entity));

            when(cloudinaryService.uploadProfilePhoto(any(), eq("t1_1")))
                    .thenReturn("https://cloudinary.com/profile.jpg");

            mockMvc.perform(multipart("/api/profile/photo")
                            .file(file)
                            .with(user(
                                    new UserType1Details(TestUsers.normalType1Domain()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.photoUrl")
                            .value("https://cloudinary.com/profile.jpg"));

            verify(userType1Repository).save(any());
        }

        @Test
        @DisplayName("Should upload profile photo for TYPE2")
        void uploadProfilePhoto_shouldUploadForType2() throws Exception {

            UserType2Entity entity = new UserType2Entity();
            entity.setId(2L);
            entity.setEmail("user@test.com");

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "profile.jpg",
                            "image/jpeg",
                            "image".getBytes());

            when(userType2Repository.findByEmail("user@test.com"))
                    .thenReturn(Optional.of(entity));

            when(cloudinaryService.uploadProfilePhoto(any(), eq("t2_2")))
                    .thenReturn("https://cloudinary.com/user.jpg");

            mockMvc.perform(multipart("/api/profile/photo")
                            .file(file)
                            .with(user(
                                    new UserType2Details(TestUsers.normalType2Domain()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE2"));

            verify(userType2Repository).save(any());
        }

        @Test
        @DisplayName("Should return 400 for unknown user type")
        void uploadProfilePhoto_shouldReturn400_whenUnknownUser() throws Exception {

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "profile.jpg",
                            "image/jpeg",
                            "img".getBytes());

            mockMvc.perform(multipart("/api/profile/photo")
                            .file(file)
                            .with(user("someone@test.com")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetProfilePhotoTests{


        @Test
        void getProfilePhoto_shouldReturnType1Photo() throws Exception {

            UserType1Entity entity = new UserType1Entity();

            entity.setEmail("admin@test.com");
            entity.setProfilePicture("https://cloud/photo.jpg");

            when(userType1Repository.findByEmail("admin@test.com"))
                    .thenReturn(Optional.of(entity));

            mockMvc.perform(get("/api/profile/photo")
                            .with(user(new UserType1Details(TestUsers.normalType1Domain()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.photoUrl")
                            .value("https://cloud/photo.jpg"));

        }

        @Test
        void getProfilePhoto_shouldReturnType2Photo() throws Exception {

            UserType2Entity entity = new UserType2Entity();

            entity.setEmail("user@test.com");
            entity.setProfilePicture("https://cloud/user.jpg");

            when(userType2Repository.findByEmail("user@test.com"))
                    .thenReturn(Optional.of(entity));

            mockMvc.perform(get("/api/profile/photo")
                            .with(user(new UserType2Details(TestUsers.normalType2Domain()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userType")
                            .value("TYPE2"));

        }
    }

    @Nested
    class DeleteProfilePhotoTests {
        @Test
        void removePhoto_shouldDeleteType1Photo() throws Exception {

            UserType1Entity entity = new UserType1Entity();

            entity.setId(1L);
            entity.setEmail("admin@test.com");
            entity.setProfilePicture("https://cloud/img.jpg");

            when(userType1Repository.findByEmail("admin@test.com"))
                    .thenReturn(Optional.of(entity));

            doNothing().when(cloudinaryService)
                    .deleteImage("multiuser/profiles/user_t1_1");

            mockMvc.perform(delete("/api/profile/photo")
                            .with(user(new UserType1Details(TestUsers.normalType1Domain()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Profile photo removed"));

            verify(cloudinaryService)
                    .deleteImage(any());

            verify(userType1Repository)
                    .save(any());
        }

        @Test
        void removePhoto_shouldDeleteType2Photo() throws Exception {

            UserType2Entity entity = new UserType2Entity();

            entity.setId(2L);
            entity.setEmail("user@test.com");
            entity.setProfilePicture("https://cloud/img.jpg");

            when(userType2Repository.findByEmail("user@test.com"))
                    .thenReturn(Optional.of(entity));

            doNothing().when(cloudinaryService)
                    .deleteImage("multiuser/profiles/user_t2_2");

            mockMvc.perform(delete("/api/profile/photo")
                            .with(user(new UserType2Details(TestUsers.normalType2Domain()))))
                    .andExpect(status().isOk());

            verify(cloudinaryService)
                    .deleteImage(any());

            verify(userType2Repository)
                    .save(any());
        }
    }
}
