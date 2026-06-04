package com.saas.billing.service;

import com.saas.billing.dto.UserDto;

import java.util.UUID;

public interface UserService {
    UserDto getUserById(UUID userId);
    UserDto getUserByEmail(String email);
    UserDto updateUser(UUID userId, UserDto userDto);
    void deactivateUser(UUID userId);
}
