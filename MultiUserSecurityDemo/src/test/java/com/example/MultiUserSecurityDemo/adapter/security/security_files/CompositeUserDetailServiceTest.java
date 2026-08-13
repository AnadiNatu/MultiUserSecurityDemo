package com.example.MultiUserSecurityDemo.adapter.security.security_files;

import com.example.MultiUserSecurityDemo.domain.model.UserType1;
import com.example.MultiUserSecurityDemo.domain.model.UserType2;
import com.example.MultiUserSecurityDemo.domain.port.UserType1Port;
import com.example.MultiUserSecurityDemo.domain.port.UserType2Port;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType1Details;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType2Details;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompositeUserDetailService Tests")
class CompositeUserDetailServiceTest {

    @Mock
    private UserType1Port userType1Port;

    @Mock
    private UserType2Port userType2Port;

    @InjectMocks
    private CompositeUserDetailService compositeUserDetailService;


    @Test
    @DisplayName("Should return TYPE1 UserDetails when TYPE1 user exists")
    void loadUserByUsername_shouldReturnType1Details_whenType1UserExists() {

        // Arrange
        UserType1 user = mock(UserType1.class);

        when(userType1Port.findByEmail("type1@test.com"))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails result =
                compositeUserDetailService
                        .loadUserByUsername("type1@test.com");

        // Assert
        assertThat(result)
                .isInstanceOf(UserType1Details.class);

        verify(userType1Port)
                .findByEmail("type1@test.com");

        verify(userType2Port, never())
                .findByEmail(anyString());
    }


    @Test
    @DisplayName("Should return TYPE2 UserDetails when TYPE1 user does not exist")
    void loadUserByUsername_shouldReturnType2Details_whenType2UserExists() {

        // Arrange
        UserType2 user = mock(UserType2.class);

        when(userType1Port.findByEmail("type2@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("type2@test.com"))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails result =
                compositeUserDetailService
                        .loadUserByUsername("type2@test.com");

        // Assert
        assertThat(result)
                .isInstanceOf(UserType2Details.class);

        verify(userType1Port)
                .findByEmail("type2@test.com");

        verify(userType2Port)
                .findByEmail("type2@test.com");
    }


    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {

        // Arrange
        when(userType1Port.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        when(userType2Port.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                compositeUserDetailService
                        .loadUserByUsername("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: missing@test.com");

        verify(userType1Port)
                .findByEmail("missing@test.com");

        verify(userType2Port)
                .findByEmail("missing@test.com");
    }
}