package com.findmyflight.findmyflight.service.emailnotificationsender;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email-notification-sender")
@Validated
@RequiredArgsConstructor
public class EmailNotificationSenderController {
    private final EmailNotificationSenderService emailNotificationSenderService;

    @GetMapping()
    public ResponseEntity<Void> send() throws MessagingException {
        emailNotificationSenderService.sendMailToSubscribers();
        return ResponseEntity.ok().build();
    }
}
