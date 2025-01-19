package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.IntegrationTest;
import com.findmyflight.findmyflight.utils.EmailMessageConverter;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Arrays;

class EmailNotificationSenderControllerTest extends IntegrationTest {
    private GreenMail greenMail;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailNotificationSenderService emailNotificationSenderService;
    @Autowired
    private SpringTemplateEngine springTemplateEngine;
    @Autowired
    EmailMessageConverter emailMessageConverter;

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.transport.protocol}")
    private String transportProtocol;

    @BeforeEach
    public void setUp() {
        ServerSetup setup = new ServerSetup(port, host, transportProtocol);
        greenMail = new GreenMail(setup);
        greenMail.setUser(username, password);
        greenMail.start();
    }

    @AfterEach
    void cleanUp() {
        flightWatcherCreator.deleteAll();
        greenMail.stop();
    }

    @Test
    void testGreenMailSetup() {
        Assertions.assertEquals(true, greenMail.isRunning());
    }

    @Nested
    class SendMailToSubscribersTest {
        @Test
        void givenFlightWatchers_thenReturnMail() throws MessagingException {
            //given
            flightWatcherCreator.createSampleWithSuspendedFalse();
            flightWatcherCreator.createSampleWithSuspendedTrue();
            emailNotificationReceiverCreator.createSample();
            emailNotificationReceiverCreator.createSample();
            //when
            //then
            webTestClient.get()
                    .uri("/email-notification-sender")
                    .exchange()
                    .expectStatus().isOk();
            var messages = greenMail.getReceivedMessages();
            var result = emailMessageConverter.convertEmail(GreenMailUtil.getBody(messages[0]));
            var expected = "Hello,Thanks for subscribe SkyDealHunter.    Your observed flights:                FROM        TO        FROM        TO        Suspended                        gdansk        rome        2025-01-19        2025-01-20        false        We wish you safety, comfortable and cheapest flight to your dream places.    Greetings,    SkyDealHunter";
            Assertions.assertEquals(1, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(expected, result);
        }
    }
}