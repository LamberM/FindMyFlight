package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.IntegrationTest;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
class EmailNotificationSenderServiceIT extends IntegrationTest {
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
            assertEquals(0, Arrays.stream(messages).toList().size());
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
            assertEquals(1, Arrays.stream(messages).toList().size());
            assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            assertEquals(loadResource("/email-result.txt"), messageResult);
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
            assertEquals(0, Arrays.stream(messages).toList().size());
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
            assertEquals(0, Arrays.stream(messages).toList().size());
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
            assertEquals(1, Arrays.stream(messages).toList().size());
            assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            assertEquals(loadResource("/email-result.txt"), messageResult);
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
            assertEquals(0, Arrays.stream(messages).toList().size());
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
            assertEquals(1, Arrays.stream(messages).toList().size());
            assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            assertEquals(loadResource("/email-result-with-two-flightResults.txt"), messageResult);
        }

        @Test
        void givenTwoEmailNotificationReceiverWithTwoActiveFlightWatchersWithFlightResult_whenSendMailToSubscribers_thenReturnTwoMailsWithTwoEmailResults() throws MessagingException {
            //given
            var emailNotificationReceiver1 = emailNotificationReceiverCreator.createSample();
            var emailNotificationReceiver2 = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver1);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var message1 = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            var message2 = convertEmailMessage(GreenMailUtil.getBody((messages[1])));
            //then
            assertEquals(2, Arrays.stream(messages).toList().size());
            assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            assertEquals("Your observed flights in SkyDealHunter", messages[1].getSubject());
            assertEquals(loadResource("/email-result.txt"), message1);
            assertEquals(loadResource("/email-result.txt"), message2);
        }

        @Test
        void givenTwoEmailNotificationReceiverWithTwoOrOneFlightWatchersAndTwoOrOneFlightResult_whenSendMailToSubscribers_thenReturnTwoMailsOneEmailResultWithTwoFlightResultAndEmailResult() throws MessagingException {
            //given
            var emailNotificationReceiver1 = emailNotificationReceiverCreator.createSample();
            var emailNotificationReceiver2 = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver1);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var message1 = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            var message2 = convertEmailMessage(GreenMailUtil.getBody((messages[1])));
            //then
            assertEquals(2, Arrays.stream(messages).toList().size());
            assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            assertEquals("Your observed flights in SkyDealHunter", messages[1].getSubject());
            assertEquals(loadResource("/email-result.txt"), message1);
            assertEquals(loadResource("/email-result-with-two-flightResults.txt"), message2);
        }

        //2 ENR 1FW 1 FR, 1 ENR 1 FW
        @Test
        void givenThreeEmailNotificationReceiversWithOneFlightWatcherWithOneOrNoneFlightResult_whenSendMailToSubscribers_ThenReturnTwoMailsWithTwoEmailResult() throws MessagingException {
            //given
            var emailNotificationReceiver1 = emailNotificationReceiverCreator.createSample();
            var emailNotificationReceiver2 = emailNotificationReceiverCreator.createSample();
            var emailNotificationReceiver3 = emailNotificationReceiverCreator.createSample();
            flightWatcherCreator.createSample(false, LocalDate.now(), emailNotificationReceiver1);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            flightResultCreator.createSample(false, emailNotificationReceiver3);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var message1 = convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            var message2 = convertEmailMessage(GreenMailUtil.getBody((messages[1])));
            //then
            assertEquals(2, Arrays.stream(messages).toList().size());
            assertEquals("Your observed flights in SkyDealHunter", messages[0].getSubject());
            assertEquals("Your observed flights in SkyDealHunter", messages[1].getSubject());
            assertEquals(loadResource("/email-result.txt"), message1);
            assertEquals(loadResource("/email-result.txt"), message2);
        }

    }
}