package com.findmyflight.findmyflight.utils;

public final class ValidationPattern {
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_]).{8,64}$";
    public static final String UUID_TOKEN_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";

    private ValidationPattern() {
    }
}
