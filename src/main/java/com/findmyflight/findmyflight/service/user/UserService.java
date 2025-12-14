package com.findmyflight.findmyflight.service.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateOrUpdateUserRequest userRequest) {
        var user = userMapper.map(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Transactional(readOnly = true)
    public Collection<UserResponse> findAll() {
        return userMapper.map(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.map(userRepository.findById(id).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional
    public UserResponse update(Long id, CreateOrUpdateUserRequest userRequest) {
        var user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        userMapper.updateFromRequest(userRequest, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
