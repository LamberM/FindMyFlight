package com.findmyflight.findmyflight.utils;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class TextConverter {
    public String convertEmailMessage(String email) {
        return email.replaceAll("(?s)(^-)(.*)(?=<!DOCTYPE html>)", "")
                .replaceAll("(?<=</html>)(?s).*", "")
                .replaceAll("\\r\\n", "\n")
                .lines()
                .map(String::strip)
                .collect(Collectors.joining("\n"))
                .strip();
    }

    public String loadResource(String path) {
        var resourceLoader = new DefaultResourceLoader();
        var resource = resourceLoader.getResource(path);
        return resourceToString(resource)
                .replaceAll("\\r\\n", "\n")
                .lines()
                .map(String::strip)
                .collect(Collectors.joining("\n"))
                .strip();
    }

    private String resourceToString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
