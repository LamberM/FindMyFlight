package com.findmyflight.findmyflight.service.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateOrUpdateUserRequest(
        @NotBlank(message = "Login should not be blank")
        @Email(message = "Invalid login email")
        String login,
        @NotBlank(message = "FullName should not be blank")
        String fullName,
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_]).{8,64}$",
                message = "Password has to contain lowercase letter, uppercase letter, digit and special character")
        String password) {
}
