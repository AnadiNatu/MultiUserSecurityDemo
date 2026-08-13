package com.example.MultiUserSecurityDemo.adapter.persistence;

import com.example.MultiUserSecurityDemo.adapter.persistence.entity.UserType2Entity;
import com.example.MultiUserSecurityDemo.adapter.persistence.mapper.UserType2Mapper;
import com.example.MultiUserSecurityDemo.adapter.persistence.repository.UserType2Repository;
import com.example.MultiUserSecurityDemo.domain.model.UserType2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserType2PersistenceAdapter Tests")
class UserType2PersistenceAdapterTest {

    @Mock
    private UserType2Repository repository;

    @Mock
    private UserType2Mapper mapper;

    @InjectMocks
    private UserType2PersistenceAdapter adapter;


    @Test
    @DisplayName("Should return domain user when email exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {

        // Arrange
        String email = "type2@test.com";

        UserType2Entity entity =
                new UserType2Entity();

        UserType2 domainUser =
                new UserType2();

        when(repository.findByEmail(email))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(domainUser);

        // Act
        Optional<UserType2> result =
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
        String email = "missing-type2@test.com";

        when(repository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserType2> result =
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
        UserType2 domainUser =
                new UserType2();

        UserType2Entity entity =
                new UserType2Entity();

        UserType2Entity savedEntity =
                new UserType2Entity();

        UserType2 savedDomainUser =
                new UserType2();

        when(mapper.toEntity(domainUser))
                .thenReturn(entity);

        when(repository.save(entity))
                .thenReturn(savedEntity);

        when(mapper.toDomain(savedEntity))
                .thenReturn(savedDomainUser);

        // Act
        UserType2 result =
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
        Long id = 20L;

        UserType2Entity entity =
                new UserType2Entity();

        UserType2 domainUser =
                new UserType2();

        when(repository.findById(id))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(domainUser);

        // Act
        Optional<UserType2> result =
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
        Optional<UserType2> result =
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
        UserType2Entity entity1 =
                new UserType2Entity();

        UserType2Entity entity2 =
                new UserType2Entity();

        UserType2 user1 =
                new UserType2();

        UserType2 user2 =
                new UserType2();

        when(repository.findAll())
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(user1);

        when(mapper.toDomain(entity2))
                .thenReturn(user2);

        // Act
        List<UserType2> result =
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
        UserType2Entity entity1 =
                new UserType2Entity();

        UserType2Entity entity2 =
                new UserType2Entity();

        UserType2 user1 =
                new UserType2();

        UserType2 user2 =
                new UserType2();

        when(repository.findByIsApprovedFalse())
                .thenReturn(List.of(entity1, entity2));

        when(mapper.toDomain(entity1))
                .thenReturn(user1);

        when(mapper.toDomain(entity2))
                .thenReturn(user2);

        // Act
        List<UserType2> result =
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
        Long id = 20L;

        // Act
        adapter.deleteById(id);

        // Assert
        verify(repository)
                .deleteById(id);

        verifyNoInteractions(mapper);
    }
}