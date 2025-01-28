package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.IntegrationTest;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

class EmailNotificationSenderServiceTest extends IntegrationTest {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailNotificationSenderService systemUnderTest;
    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @BeforeEach
    public void setUp() {
        greenMail.setUser(username, password);
    }

    @AfterEach
    void cleanUp() {
        flightWatcherCreator.deleteAll();
        emailNotificationReceiverCreator.deleteAll();
    }

    @Nested
    class SendMailToSubscribersTest {
        @Test
        void givenFlightWatchers_thenReturnMail() throws MessagingException {
            //given
            emailNotificationReceiverCreator.createSampleWithSameMail();
            flightWatcherCreator.createSampleWithSameMail(false);
            flightWatcherCreator.createSampleWithSameMail(false);
            flightWatcherCreator.createSampleWithSameMail(true);
            flightWatcherCreator.createSample(true);
            flightWatcherCreator.createSample(true);
            flightWatcherCreator.createSample(false);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var firstMessageResult = convertEmail(GreenMailUtil.getBody(messages[0]));
            var secondMessageResult = convertEmail(GreenMailUtil.getBody(messages[1]));
            //then
            Assertions.assertEquals(2, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(expected("/first-email-result.txt"), firstMessageResult);
            Assertions.assertEquals(expected("/second-email-result.txt"), secondMessageResult);
        }
    }

    private String expected(String path) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return resourceToString(resource);
    }

    private String resourceToString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String convertEmail(String email) {
        return email.replaceAll("(?s)(^-)(.*)(?=<!DOCTYPE html>)", "").replaceAll("(?=</html>)(?s)(.*)", "").strip();
    }
}