package com.findmyflight.findmyflight.service.emailnotificationreceiver;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/email-notification-receiver")
@Validated
@RequiredArgsConstructor
public class EmailNotificationReceiverController {
    private final EmailNotificationReceiverService emailNotificationReceiverService;

    @GetMapping()
    public ResponseEntity<Collection<EmailNotificationReceiverResponse>> findAll() {
        return ResponseEntity.ok(emailNotificationReceiverService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailNotificationReceiverResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(emailNotificationReceiverService.findById(id));
    }

    @PostMapping()
    public ResponseEntity<EmailNotificationReceiverResponse> create(@Valid @RequestBody CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest) {
        return ResponseEntity.ok(emailNotificationReceiverService.create(createOrUpdateEmailNotificationReceiverRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailNotificationReceiverResponse> update(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest) {
        return ResponseEntity.ok(emailNotificationReceiverService.update(id, createOrUpdateEmailNotificationReceiverRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        emailNotificationReceiverService.delete(id);
        return ResponseEntity.ok().build();
    }
}
