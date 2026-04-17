package com.findmyflight.findmyflight.service.passwordreset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgetPasswordRequest(
        @NotBlank(message = "Login should not be blank")
        @Email(message = "Invalid login email")
        String login) {
}
