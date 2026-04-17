package com.findmyflight.findmyflight.samplecreator;

import com.findmyflight.findmyflight.service.passwordreset.PasswordResetToken;
import com.findmyflight.findmyflight.service.passwordreset.PasswordResetTokenRepository;
import com.findmyflight.findmyflight.service.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenCreator {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public PasswordResetToken createSample(User user) {
        var passwordResetToken = PasswordResetToken.builder()
                .token(generateSecureToken())
                .user(user)
                .build();
        return passwordResetTokenRepository.save(passwordResetToken);
    }

    public String generateSecureToken() {
        return UUID.randomUUID().toString();
    }

    public void deleteAll() {
        passwordResetTokenRepository.deleteAll();
    }
}
