package com.findmyflight.findmyflight.utils;

import org.springframework.stereotype.Component;

@Component
public class EmailMessageConverter {
    public String convertEmail(String email) {
        return email.replaceAll("(?s)(^-)(.*)(?=<!DOCTYPE html>)", "").replaceAll("(?=</html>)(?s)(.*)", "").replaceAll("(<.*?>)", "").strip().replaceAll("\\r\\n", "");
    }
}
