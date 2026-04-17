package com.findmyflight.findmyflight.service.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest userRequest) {
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
    public UserResponse update(Long id, UpdateUserRequest userRequest) {
        var user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        userMapper.updateFromRequest(userRequest, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional
    public void updatePassword(Long id, String rawPassword) {
        var user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}
