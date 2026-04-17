package com.findmyflight.findmyflight.service.passwordreset;

import com.findmyflight.findmyflight.utils.ValidationPattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank
        @Pattern(
                regexp = ValidationPattern.UUID_TOKEN_PATTERN,
                message = "Invalid token format")
        String token,
        @NotBlank
        @Pattern(
                regexp = ValidationPattern.PASSWORD_PATTERN,
                message = "Password has to contain lowercase letter, uppercase letter, digit and special character")
        String newPassword) {
}
