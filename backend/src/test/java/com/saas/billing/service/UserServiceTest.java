package com.saas.billing.service;

import com.saas.billing.dto.UserDto;
import com.saas.billing.entity.User;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("john@test.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        userDto = new UserDto();
        userDto.setId(userId);
        userDto.setEmail("john@test.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
    }

    @Test
    void shouldGetUserByIdSuccessfully() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@test.com", result.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldGetUserByEmailSuccessfully() {

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));

        UserDto result =
                userService.getUserByEmail("john@test.com");

        assertNotNull(result);
        assertEquals("john@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(userRepository)
                .findByEmail("john@test.com");
    }

    @Test
    void shouldThrowWhenUserNotFoundByEmail() {

        when(userRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByEmail("john@test.com")
        );

        verify(userRepository)
                .findByEmail("john@test.com");
    }
        @Test
    void shouldUpdateUserSuccessfully() {

        UserDto updateDto = new UserDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("john@test.com", result.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUpdatingUserNotFound() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(userId, userDto)
        );

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeactivateUserSuccessfully() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                userService.deactivateUser(userId));

        assertFalse(user.getIsActive());

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenDeactivatingUserNotFound() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deactivateUser(userId)
        );

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
}