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

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailNotificationSenderServiceIT extends IntegrationTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);
    @Autowired
    private EmailNotificationSenderService systemUnderTest;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${app.password-reset.url}")
    private String passwordResetUrl;
    private static final String SEND_MAIL_TO_SUBSCRIBERS_SUBJECT = "Your observed flights in SkyDealHunter";
    private static final String SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_PATH = "/send-mail-to-subscribers-email-result.txt";
    private static final String SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_WITH_TWO_FLIGHT_RESULTS_PATH = "/send-mail-to-subscribers-email-result-with-two-flightResults.txt";


    @BeforeEach
    public void setUp() {
        greenMail.setUser(username, password);
    }

    @AfterEach
    void cleanUp() {
        flightResultCreator.deleteAll();
        flightWatcherCreator.deleteAll();
        emailNotificationReceiverCreator.deleteAll();
        userCreator.deleteAll();
        passwordResetTokenCreator.deleteAll();
    }

    @Nested
    class SendMailToSubscribersIT {
        @Test
        void givenNothing_whenSendMailToSubscribers_thenReturnEmptyEmailList() {
            //given
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            //then
            assertEquals(0, Arrays.stream(messages).toList().size());
        }

        @Test
        void givenEmailNotificationReceiverWithActiveFlightWatcherWithFlightResult_whenSendMailToSubscribers_thenReturnMessage() throws
                MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            assertEquals(1, Arrays.stream(messages).toList().size());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[0].getSubject());
            assertEquals(textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_PATH), messageResult);
        }

        @Test
        void givenEmailNotificationReceiverWithNotActiveFlightWatcher_whenSendMailToSubscribers_thenReturnEmptyEmailList() {
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
        void givenEmailNotificationReceiverWithActiveFlightWatcherWithoutFlightResult_whenSendMailToSubscribers_thenReturnEmptyEmailList() {
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
        void givenEmailNotificationReceiverWithTwoFlightWatchersOneActiveOneNotWithFlightResult_whenSendMailToSubscribers_thenReturnMessage() throws
                MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver);
            flightWatcherCreator.createSample(true, LocalDate.now(), emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            assertEquals(1, Arrays.stream(messages).toList().size());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[0].getSubject());
            assertEquals(textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_PATH), messageResult);
        }

        @Test
        void givenEmailNotificationReceiverWithTwoFlightWatchersOneActiveOneNotWithoutFlightResult_whenSendMailToSubscribers_thenReturnEmptyEmailSizeList() {
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
        void givenEmailNotificationReceiverWithTwoActiveFlightWatchersSameEmailNotificationReceiverWithFlightResult_whenSendMailToSubscribers_thenReturnOneMail() throws
                MessagingException {
            //given
            var emailNotificationReceiver = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver);
            flightResultCreator.createSample(false, emailNotificationReceiver);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var messageResult = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            //then
            assertEquals(1, Arrays.stream(messages).toList().size());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[0].getSubject());
            assertEquals(textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_WITH_TWO_FLIGHT_RESULTS_PATH),
                    messageResult);
        }

        @Test
        void givenTwoEmailNotificationReceiverWithTwoActiveFlightWatchersWithFlightResult_whenSendMailToSubscribers_thenReturnTwoMailsWithTwoEmailResults() throws
                MessagingException {
            //given
            var emailNotificationReceiver1 = emailNotificationReceiverCreator.createSample();
            var emailNotificationReceiver2 = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver1);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var message1 = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            var message2 = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[1])));
            //then
            var expectedMessage = textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_PATH);

            assertEquals(2, Arrays.stream(messages).toList().size());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[0].getSubject());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[1].getSubject());
            assertEquals(expectedMessage, message1);
            assertEquals(expectedMessage, message2);
        }

        @Test
        void givenTwoEmailNotificationReceiverWithTwoOrOneFlightWatchersAndTwoOrOneFlightResult_whenSendMailToSubscribers_thenReturnTwoMailsOneEmailResultWithTwoFlightResultAndEmailResult() throws
                MessagingException {
            //given
            var emailNotificationReceiver1 = emailNotificationReceiverCreator.createSample();
            var emailNotificationReceiver2 = emailNotificationReceiverCreator.createSample();
            flightResultCreator.createSample(false, emailNotificationReceiver1);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            flightResultCreator.createSample(false, emailNotificationReceiver2);
            //when
            systemUnderTest.sendMailToSubscribers();
            var messages = greenMail.getReceivedMessages();
            var message1 = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            var message2 = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[1])));
            //then
            assertEquals(2, Arrays.stream(messages).toList().size());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[0].getSubject());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[1].getSubject());
            assertEquals(textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_PATH), message1);
            assertEquals(textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_WITH_TWO_FLIGHT_RESULTS_PATH),
                    message2);
        }

        @Test
        void givenThreeEmailNotificationReceiversWithOneFlightWatcherWithOneOrNoneFlightResult_whenSendMailToSubscribers_ThenReturnTwoMailsWithTwoEmailResult() throws
                MessagingException {
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
            var message1 = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[0])));
            var message2 = textConverter.convertEmailMessage(GreenMailUtil.getBody((messages[1])));
            //then
            var expectedMessage = textConverter.loadResource(SEND_MAIL_TO_SUBSCRIBERS_EMAIL_RESULT_PATH);

            assertEquals(2, Arrays.stream(messages).toList().size());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[0].getSubject());
            assertEquals(SEND_MAIL_TO_SUBSCRIBERS_SUBJECT, messages[1].getSubject());
            assertEquals(expectedMessage, message1);
            assertEquals(expectedMessage, message2);
        }

    }

    @Nested
    class SendForgotPasswordMailIT {
        @Test
        void givenUserAndCorrectLogin_whenForgetPassword_thenGetOk() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            var email = givenUser.getLogin();
            var link = passwordResetUrl + "?token=testToken";
            //when
            systemUnderTest.sendForgotPasswordMail(email, link);
            //then
            var receivedMessages = greenMail.getReceivedMessages();
            Assertions.assertEquals(1, receivedMessages.length);
            var message = textConverter.convertEmailMessage(GreenMailUtil.getBody((receivedMessages[0])));
            Assertions.assertEquals(textConverter.loadResource("/forget-password-email-result.txt"), message);
        }
    }
}