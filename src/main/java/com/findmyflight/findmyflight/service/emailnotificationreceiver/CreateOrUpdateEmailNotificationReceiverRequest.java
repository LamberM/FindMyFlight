package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateEmailNotificationReceiverRequest(
        @Column(unique = true) @NotBlank(message = "Address should not be blank")
        @Email(message = "Invalid address email") String address) {
}
