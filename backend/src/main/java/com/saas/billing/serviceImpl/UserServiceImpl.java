package com.saas.billing.serviceImpl;

import com.saas.billing.dto.UserDto;
import com.saas.billing.entity.User;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return DtoMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return DtoMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(UUID userId, UserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        User saved = userRepository.save(user);
        return DtoMapper.toUserDto(saved);
    }

    @Override
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }
}
