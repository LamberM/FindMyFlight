package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailNotificationReceiverDto(@NotBlank(message = "Address should not be blank")
                                           @Email(message = "Invalid address email") String address) {
}
