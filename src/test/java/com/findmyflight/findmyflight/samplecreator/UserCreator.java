package com.findmyflight.findmyflight.samplecreator;

import com.findmyflight.findmyflight.service.user.User;
import com.findmyflight.findmyflight.service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class UserCreator {
    private static final Random RANDOM = new Random();
    private static final String EMAIL_SAMPLE_PATTERN = "test-email-%d@test.com";
    private static final String PASSWORD_SAMPLE_PATTERN = "%dtestPassword!";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createSample(String fullName) {
        var user = User.builder()
                .login(generateEmail())
                .password(passwordEncoder.encode(generatePassword()))
                .fullName(fullName)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public void deleteAll() {
        userRepository.deleteAll();
    }

    private String generateEmail() {
        return String.format(EMAIL_SAMPLE_PATTERN, RANDOM.nextInt());
    }

    private String generatePassword() {
        return String.format(PASSWORD_SAMPLE_PATTERN, RANDOM.nextInt());
    }

}
