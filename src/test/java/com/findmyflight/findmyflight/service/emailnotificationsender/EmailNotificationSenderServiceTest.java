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

import java.time.LocalDate;
import java.util.Arrays;

class EmailNotificationSenderServiceTest extends IntegrationTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);
    @Autowired
    private EmailNotificationSenderService systemUnderTest;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;

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

    private String convertEmailMessage(String email) {
        return email.replaceAll("(?s)(^-)(.*)(?=<!DOCTYPE html>)", "").replaceAll("(?<=</html>)(?s).*", "").strip();
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
        void givenEmailNotificationReceiverWithActiveFlightWatcherWithFlightResult_whenSendMailToSubscribers_thenReturnMessage() throws MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            Assertions.assertEquals(1, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(loadResource("/email-result.txt"), messageResult);
        }

        @Test
        void givenEmailNotificationReceiverWithNotActiveFlightWatcher_whenSendMailToSubscribers_thenReturnEmptyEmailList() throws MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightWatcherCreator.createSample(true, LocalDate.now(), emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithActiveFlightWatcherWithoutFlightResult_whenSendMailToSubscribers_thenReturnEmptyEmailList() throws MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightWatcherCreator.createSample(false, LocalDate.now(), emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithTwoFlightWatchersOneActiveOneNotWithFlightResult_whenSendMailToSubscribers_thenReturnMessage() throws MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver);
            flightWatcherCreator.createSample(true, LocalDate.now(), emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            Assertions.assertEquals(1, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(loadResource("/email-result.txt"), messageResult);
        }

        @Test
        void givenEmailNotificationReceiverWithTwoFlightWatchersOneActiveOneNotWithoutFlightResult_whenSendMailToSubscribers_thenReturnEmptyEmailSizeList() throws MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightWatcherCreator.createSample(true, LocalDate.now(), emailNotificationReceiver);
            flightWatcherCreator.createSample(false, LocalDate.now(), emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            Assertions.assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithTwoActiveFlightWatchersSameEmailNotificationReceiverWithFlightResult_whenSendMailToSubscribers_thenReturnOneMail() throws MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver);
            flightResultCreator.createSample(false, emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            Assertions.assertEquals(1, Arrays.stream(messages).toList().size());
            Assertions.assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            Assertions.assertEquals(loadResource("/email-result-with-two-flightResults"), messageResult);
        }
    }
}