package com.findmyflight.findmyflight;

import com.findmyflight.findmyflight.config.TestcontainersConfig;
import com.findmyflight.findmyflight.samplecreator.EmailNotificationReceiverCreator;
import com.findmyflight.findmyflight.samplecreator.FlightResultCreator;
import com.findmyflight.findmyflight.samplecreator.FlightWatcherCreator;
import com.findmyflight.findmyflight.utils.TransactionHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfig.class)
public abstract class IntegrationTest {
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected TransactionHelper transactionHelper;
    @Autowired
    protected FlightWatcherCreator flightWatcherCreator;
    @Autowired
    protected EmailNotificationReceiverCreator emailNotificationReceiverCreator;
    @Autowired
    protected FlightResultCreator flightResultCreator;

    public String loadResource(String path) {
        var resourceLoader = new DefaultResourceLoader();
        var resource = resourceLoader.getResource(path);
        return resourceToString(resource);
    }

    private String resourceToString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}