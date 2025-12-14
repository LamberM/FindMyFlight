package com.findmyflight.findmyflight.service.user;

import java.time.LocalDateTime;

public record UserResponse(Long id, String login, String fullName, LocalDateTime createdAt, LocalDateTime updatedAt,
                           LocalDateTime lastLoginAt) {
}
