package com.example.MultiUserSecurityDemo.adapter.persistence;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType1Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.mapper.UserType1Mapper;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.UserType1Repository;
import com.example.MultiUserSecurityDemo.domain.model.UserType1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserType1PersistenceAdapter Tests")
class UserType1PersistenceAdapterTest {

    @Mock
    private UserType1Repository repository;

    @Mock
    private UserType1Mapper mapper;

    @InjectMocks
    private UserType1PersistenceAdapter adapter;

    @Test
    @DisplayName("Should return domain user when email exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {

        // Arrange
        String email = "user@test.com";

        UserType1Entity entity =
                new UserType1Entity();

        UserType1 domainUser =
                new UserType1();

        when(repository.findByEmail(email))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(domainUser);

        // Act
        Optional<UserType1> result =
                adapter.findByEmail(email);

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get())
                .isSameAs(domainUser);

        verify(repository)
                .findByEmail(email);

        verify(mapper)
                .toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        // Arrange
        String email = "missing@test.com";

        when(repository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserType1> result =
                adapter.findByEmail(email);

        // Assert
        assertThat(result)
                .isEmpty();

        verify(repository)
                .findByEmail(email);

        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should map, save and return domain user")
    void save_shouldMapSaveAndReturnDomainUser() {

        // Arrange
        UserType1 domainUser =
                new UserType1();

        UserType1Entity entity =
                new UserType1Entity();

        UserType1Entity savedEntity =
                new UserType1Entity();

        UserType1 savedDomainUser =
                new UserType1();

        when(mapper.toEntity(domainUser))
                .thenReturn(entity);

        when(repository.save(entity))
                .thenReturn(savedEntity);

        when(mapper.toDomain(savedEntity))
                .thenReturn(savedDomainUser);

        // Act
        UserType1 result =
                adapter.save(domainUser);

        // Assert
        assertThat(result)
                .isSameAs(savedDomainUser);

        verify(mapper)
                .toEntity(domainUser);

        verify(repository)
                .save(entity);

        verify(mapper)
                .toDomain(savedEntity);
    }

    @Test
    @DisplayName("Should return domain user when ID exists")
    void findById_shouldReturnUser_whenIdExists() {

        // Arrange
        Long id = 10L;

        UserType1Entity entity =
                new UserType1Entity();

        UserType1 domainUser =
                new UserType1();

        when(repository.findById(id))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(domainUser);

        // Act
        Optional<UserType1> result =
                adapter.findById(id);

        // Assert
        assertThat(result)
                .isPresent();

        assertThat(result.get())
                .isSameAs(domainUser);

        verify(repository)
                .findById(id);

        verify(mapper)
                .toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {

        // Arrange
        Long id = 999L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserType1> result =
                adapter.findById(id);

        // Assert
        assertThat(result)
                .isEmpty();

        verify(repository)
                .findById(id);

        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should return all users mapped to domain objects")
    void findAll_shouldReturnAllUsers() {

        // Arrange
        UserType1Entity entity1 =
                new UserType1Entity();

        UserType1Entity entity2 =
                new UserType1Entity();

        UserType1 user1 =
                new UserType1();

        UserType1 user2 =
                new UserType1();

        when(repository.findAll())
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(user1);

        when(mapper.toDomain(entity2))
                .thenReturn(user2);

        // Act
        List<UserType1> result =
                adapter.findAll();

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(user1, user2);

        verify(repository)
                .findAll();

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }

    @Test
    @DisplayName("Should return unapproved users mapped to domain objects")
    void findByIsApprovedFalse_shouldReturnUnapprovedUsers() {

        // Arrange
        UserType1Entity entity1 =
                new UserType1Entity();

        UserType1Entity entity2 =
                new UserType1Entity();

        UserType1 user1 =
                new UserType1();

        UserType1 user2 =
                new UserType1();

        when(repository.findByIsApprovedFalse())
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(user1);

        when(mapper.toDomain(entity2))
                .thenReturn(user2);

        // Act
        List<UserType1> result =
                adapter.findByIsApprovedFalse();

        // Assert
        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(user1, user2);

        verify(repository)
                .findByIsApprovedFalse();

        verify(mapper)
                .toDomain(entity1);

        verify(mapper)
                .toDomain(entity2);
    }


    @Test
    @DisplayName("Should delete user by ID")
    void deleteById_shouldDelegateToRepository() {

        // Arrange
        Long id = 10L;

        // Act
        adapter.deleteById(id);

        // Assert
        verify(repository)
                .deleteById(id);

        verifyNoInteractions(mapper);
    }
}
