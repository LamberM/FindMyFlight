package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.IntegrationTest;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcher;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        flightResultCreator.deleteAll();
        flightWatcherCreator.deleteAll();
        emailNotificationReceiverCreator.deleteAll();
    }

    @Nested
    class SendMailToSubscribersTest {
        @Test
        void givenNothing_whenSendMailToSubscribers_thenReturnEmptyEmailList() throws MessagingException {
            //given
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithActiveFlightWatcherWithFlightResult_whenSendMailToSubscribers_thenReturnMessage() throws MessagingException, UnsupportedEncodingException {
            //given
            var flightResult = flightResultCreator.createSample(false);
            var flightWatcher = flightResult.getFlightWatcher();
            List<FlightWatcher> flightWatcherList = new ArrayList<>();
            flightWatcherList.add(flightWatcher);
            emailNotificationReceiverCreator.createSample(flightWatcherList);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            Assertions.assertEquals(1, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(textConverter.resourceLoader("/email-result.txt"), messageResult);
        }

        @Test
        void givenEmailNotificationReceiverWithNotActiveFlightWatcher_whenSendMailToSubscribers_thenReturnEmptyEmailList() throws MessagingException {
            //given
            flightWatcherCreator.createSample(true);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithActiveFlightWatcherWithoutFlightResult_whenSendMailToSubscribers_thenReturnEmptyEmailList() throws MessagingException {
            //given
            var flightWatcher = flightWatcherCreator.createSample(false);
            List<FlightWatcher> flightWatcherList = new ArrayList<>();
            flightWatcherList.add(flightWatcher);
            emailNotificationReceiverCreator.createSample(flightWatcherList);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithTwoFlightWatchersOneActiveOneNotWithFlightResult_whenSendMailToSubscribers_thenReturnMessage() throws MessagingException, UnsupportedEncodingException {
            //given
            var flightResult = flightResultCreator.createSample(false);
            var flightWatcher1 = flightResult.getFlightWatcher();
            var flightWatcher2 = flightWatcherCreator.createSample(true);
            List<FlightWatcher> flightWatcherList = new ArrayList<>();
            flightWatcherList.add(flightWatcher1);
            flightWatcherList.add(flightWatcher2);
            emailNotificationReceiverCreator.createSample(flightWatcherList);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            Assertions.assertEquals(1, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(textConverter.resourceLoader("/email-result.txt"), messageResult);
        }

        @Test
        void givenEmailNotificationReceiverWithTwoFlightWatchersOneActiveOneNotWithoutFlightResult_whenSendMailToSubscribers_thenReturnEmptyEmailSizeList() throws MessagingException {
            //given
            var flightWatcher1 = flightWatcherCreator.createSample(true);
            var flightWatcher2 = flightWatcherCreator.createSample(false);
            List<FlightWatcher> flightWatcherList = new ArrayList<>();
            flightWatcherList.add(flightWatcher1);
            flightWatcherList.add(flightWatcher2);
            emailNotificationReceiverCreator.createSample(flightWatcherList);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }
    }

    private String convertEmailMessage(String email) {
        var convertEmailMessage = email.replaceAll("(?s)(^-)(.*)(?=<!DOCTYPE html>)", "").replaceAll("(?<=</html>)(?s).*", "").strip();
        return convertEmailMessage.replaceAll("\\r\\n?", "\n");
    }
}