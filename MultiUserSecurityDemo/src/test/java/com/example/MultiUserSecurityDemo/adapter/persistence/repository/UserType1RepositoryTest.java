package com.example.MultiUserSecurityDemo.adapter.persistence.repository;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType1Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserType1Repository Tests")
class UserType1RepositoryTest {

    @Autowired
    private UserType1Repository userType1Repository;


    @Test
    @DisplayName("Should find user by email when user exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {

        // Arrange
        UserType1Entity user = UserType1Entity.builder()
                .fname("Admin")
                .lname("User")
                .email("admin@test.com")
                .password("encoded-password")
                .phoneNumber("9876543210")
                .role("ADMIN_TYPE1")
                .isApproved(true)
                .emailVerified(true)
                .build();

        userType1Repository.save(user);

        // Act
        Optional<UserType1Entity> result =
                userType1Repository.findByEmail("admin@test.com");

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get().getEmail())
                .isEqualTo("admin@test.com");

        assertThat(result.get().getFname())
                .isEqualTo("Admin");
    }


    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        // Act
        Optional<UserType1Entity> result =
                userType1Repository.findByEmail("missing@test.com");

        // Assert
        assertThat(result)
                .isEmpty();
    }


    @Test
    @DisplayName("Should find user by phone number when phone exists")
    void findByPhoneNumber_shouldReturnUser_whenPhoneExists() {

        // Arrange
        UserType1Entity user = UserType1Entity.builder()
                .fname("John")
                .lname("Doe")
                .email("john@test.com")
                .password("encoded-password")
                .phoneNumber("9999999999")
                .role("USER")
                .isApproved(true)
                .emailVerified(true)
                .build();

        userType1Repository.save(user);

        // Act
        Optional<UserType1Entity> result =
                userType1Repository.findByPhoneNumber("9999999999");

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get().getEmail())
                .isEqualTo("john@test.com");

        assertThat(result.get().getPhoneNumber())
                .isEqualTo("9999999999");
    }


    @Test
    @DisplayName("Should return empty when phone number does not exist")
    void findByPhoneNumber_shouldReturnEmpty_whenPhoneDoesNotExist() {

        // Act
        Optional<UserType1Entity> result =
                userType1Repository.findByPhoneNumber("1111111111");

        // Assert
        assertThat(result)
                .isEmpty();
    }


    @Test
    @DisplayName("Should return only unapproved users")
    void findByIsApprovedFalse_shouldReturnOnlyUnapprovedUsers() {

        // Arrange
        UserType1Entity approvedUser = UserType1Entity.builder()
                .fname("Approved")
                .lname("User")
                .email("approved@test.com")
                .password("encoded-password")
                .phoneNumber("9876543210")
                .role("USER")
                .isApproved(true)
                .emailVerified(true)
                .build();

        UserType1Entity unapprovedUser = UserType1Entity.builder()
                .fname("Pending")
                .lname("User")
                .email("pending@test.com")
                .password("encoded-password")
                .phoneNumber("9876543211")
                .role("USER")
                .isApproved(false)
                .emailVerified(false)
                .build();

        userType1Repository.save(approvedUser);
        userType1Repository.save(unapprovedUser);

        // Act
        List<UserType1Entity> result =
                userType1Repository.findByIsApprovedFalse();

        // Assert
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getEmail())
                .isEqualTo("pending@test.com");

        assertThat(result.get(0).isApproved())
                .isFalse();
    }
}
