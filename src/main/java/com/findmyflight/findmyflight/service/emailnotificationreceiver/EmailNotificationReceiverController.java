package com.findmyflight.findmyflight.service.emailnotificationreceiver;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/emailNotificationReceiver")
@Validated
@RequiredArgsConstructor
public class EmailNotificationReceiverController {
    private final EmailNotificationReceiverService service;

    @PostMapping("/create")
    public ResponseEntity createEmailNotificationReceiver(@Valid @RequestBody EmailNotificationReceiverDto emailNotificationReceiverDto) {
        service.createEmailNotificationReceiver(emailNotificationReceiverDto);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<EmailNotificationReceiver>> getAllEmailNotificationReceivers() {
        return ResponseEntity.ok(service.getAllEmailNotificationReceivers());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity editEmailNotificationReceiver(@PathVariable("id") Long id,
                                                        @Valid @RequestBody EmailNotificationReceiverDto emailNotificationReceiverDto) {
        service.editEmailNotificationReceiver(id, emailNotificationReceiverDto);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity deleteEmailNotificationReceiver(@PathVariable("id") Long id) {
        service.deleteEmailNotificationReceiver(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
