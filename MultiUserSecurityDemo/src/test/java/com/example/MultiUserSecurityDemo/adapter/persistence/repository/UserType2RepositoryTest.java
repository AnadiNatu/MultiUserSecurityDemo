package com.example.MultiUserSecurityDemo.adapter.persistence.repository;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType2Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserType2Repository Tests")
class UserType2RepositoryTest {

    @Autowired
    private UserType2Repository userType2Repository;


    @Test
    @DisplayName("Should find user by email when user exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {

        // Arrange
        UserType2Entity user = UserType2Entity.builder()
                .fname("Admin")
                .lname("Type2")
                .email("admin2@test.com")
                .password("encoded-password")
                .phoneNumber("9876543210")
                .role("ADMIN_TYPE2")
                .isApproved(true)
                .emailVerified(true)
                .build();

        userType2Repository.save(user);

        // Act
        Optional<UserType2Entity> result =
                userType2Repository.findByEmail("admin2@test.com");

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get().getEmail())
                .isEqualTo("admin2@test.com");

        assertThat(result.get().getRole())
                .isEqualTo("ADMIN_TYPE2");
    }


    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        // Act
        Optional<UserType2Entity> result =
                userType2Repository.findByEmail("missing@test.com");

        // Assert
        assertThat(result)
                .isEmpty();
    }


    @Test
    @DisplayName("Should return only unapproved users")
    void findByIsApprovedFalse_shouldReturnOnlyUnapprovedUsers() {

        // Arrange
        UserType2Entity approvedUser = UserType2Entity.builder()
                .fname("Approved")
                .lname("User")
                .email("approved2@test.com")
                .password("encoded-password")
                .phoneNumber("9999999991")
                .role("USER_TYPE2")
                .isApproved(true)
                .emailVerified(true)
                .build();

        UserType2Entity unapprovedUser = UserType2Entity.builder()
                .fname("Pending")
                .lname("User")
                .email("pending2@test.com")
                .password("encoded-password")
                .phoneNumber("9999999992")
                .role("USER_TYPE2")
                .isApproved(false)
                .emailVerified(false)
                .build();

        userType2Repository.save(approvedUser);
        userType2Repository.save(unapprovedUser);

        // Act
        List<UserType2Entity> result =
                userType2Repository.findByIsApprovedFalse();

        // Assert
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getEmail())
                .isEqualTo("pending2@test.com");

        assertThat(result.get(0).isApproved())
                .isFalse();
    }


    @Test
    @DisplayName("Should return empty when all users are approved")
    void findByIsApprovedFalse_shouldReturnEmpty_whenNoPendingUsers() {

        // Arrange
        UserType2Entity approvedUser = UserType2Entity.builder()
                .fname("Approved")
                .lname("User")
                .email("approved-only@test.com")
                .password("encoded-password")
                .role("USER_TYPE2")
                .isApproved(true)
                .emailVerified(true)
                .build();

        userType2Repository.save(approvedUser);

        // Act
        List<UserType2Entity> result =
                userType2Repository.findByIsApprovedFalse();

        // Assert
        assertThat(result)
                .isEmpty();
    }
}
