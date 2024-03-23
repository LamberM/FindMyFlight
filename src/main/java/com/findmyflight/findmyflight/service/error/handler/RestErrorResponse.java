package com.findmyflight.findmyflight.service.error.handler;

public record RestErrorResponse(int httpCode, String message) {
}
