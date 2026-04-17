package com.findmyflight.findmyflight.service.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateUserRequest(@NotBlank(message = "FullName should not be blank") String fullName) {
}
