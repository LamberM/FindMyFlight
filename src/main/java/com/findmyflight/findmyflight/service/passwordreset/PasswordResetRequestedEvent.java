package com.findmyflight.findmyflight.service.passwordreset;

public record PasswordResetRequestedEvent(String email, String link) {}
