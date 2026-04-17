package com.findmyflight.findmyflight.service.passwordreset;

import com.findmyflight.findmyflight.service.error.handler.InvalidTokenException;
import com.findmyflight.findmyflight.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;
    @Value("${app.password-reset.url}")
    private String passwordResetUrl;
    @Value("${app.password-reset-token.expiration}")
    private Integer tokenExpiration;

    @Scheduled(cron = "${app.password-reset-token.deleting}")
    @Transactional
    public void purgeExpiredTokens() {
        var cutoff = LocalDateTime.now().minusMinutes(tokenExpiration);
        passwordResetTokenRepository.deleteAllExpiredBefore(cutoff);
    }

    @Transactional
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        var userOptional = userService.findByLogin(forgetPasswordRequest.login());

        if (userOptional.isEmpty()) {
            log.info("forget password requested for inexistent user");
            return;
        }

        var user = userOptional.get();
        passwordResetTokenRepository.deleteByUser(user);

        var token = generateSecureToken();
        var passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getLogin(), passwordResetUrl + "?token=" + token));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        var token = resetPasswordRequest.token();
        var passwordResetToken = validatePasswordResetToken(token);

        var user = passwordResetToken.getUser();
        userService.updatePassword(user.getId(), resetPasswordRequest.newPassword());

        passwordResetTokenRepository.delete(passwordResetToken);
    }

    private PasswordResetToken validatePasswordResetToken(String token) {
        var passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token. Try again"));
        if (passwordResetToken.isExpired(tokenExpiration)) {
            throw new InvalidTokenException("Token expired. Try again");
        }
        return passwordResetToken;
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString();
    }
}
