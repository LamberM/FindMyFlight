package com.findmyflight.findmyflight.service.user;

import com.findmyflight.findmyflight.utils.ValidationPattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @NotBlank(message = "Login should not be blank")
        @Email(message = "Invalid login email")
        String login,
        @NotBlank(message = "FullName should not be blank")
        String fullName,
        @Pattern(
                regexp = ValidationPattern.PASSWORD_PATTERN,
                message = "Password has to contain lowercase letter, uppercase letter, digit and special character")
        String password) {
}
