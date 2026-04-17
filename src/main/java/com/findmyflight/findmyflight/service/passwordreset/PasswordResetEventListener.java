package com.findmyflight.findmyflight.service.passwordreset;

import com.findmyflight.findmyflight.service.emailnotificationsender.EmailNotificationSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PasswordResetEventListener {
    private final EmailNotificationSenderService emailNotificationSenderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        emailNotificationSenderService.sendForgotPasswordMail(event.email(), event.link());
    }
}

