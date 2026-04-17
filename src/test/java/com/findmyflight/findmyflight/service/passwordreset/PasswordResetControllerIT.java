package com.findmyflight.findmyflight.service.passwordreset;

import com.findmyflight.findmyflight.IntegrationTest;
import com.findmyflight.findmyflight.service.emailnotificationsender.EmailNotificationSenderService;
import com.findmyflight.findmyflight.service.error.handler.InvalidTokenException;
import com.findmyflight.findmyflight.service.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordResetControllerIT extends IntegrationTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    EmailNotificationSenderService emailNotificationSenderService;

    @AfterEach
    void cleanUp() {
        passwordResetTokenCreator.deleteAll();
        userCreator.deleteAll();
    }

    @Nested
    class ForgetPasswordIT {

        @Test
        void givenIncorrectLogin_whenForgetPassword_thenGetBadRequest() {
            //given
            userCreator.createSample("John Doe");
            var incorrectLogin = "test.pl";
            var forgetPasswordRequest = new ForgetPasswordRequest(incorrectLogin);
            //when
            //then
            webTestClient.post()
                    .uri("/password/forget")
                    .bodyValue(forgetPasswordRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenNotExistingLogin_whenForgetPassword_thenGetOk() {
            //given
            userCreator.createSample("John Doe");
            var notExistingLogin = "test@test.pl";
            var forgetPasswordRequest = new ForgetPasswordRequest(notExistingLogin);
            //when
            //then
            webTestClient.post()
                    .uri("/password/forget")
                    .bodyValue(forgetPasswordRequest)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        void givenUserAndCorrectLogin_whenForgetPassword_thenGetOk() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            var correctLogin = givenUser.getLogin();
            var forgetPasswordRequest = new ForgetPasswordRequest(correctLogin);
            //when
            //then
            webTestClient.post()
                    .uri("/password/forget")
                    .bodyValue(forgetPasswordRequest)
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    class ResetPasswordIT {

        @Test
        void givenUserAndCreateCorrectPasswordResetToken_whenResetPassword_thenReturnOk() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            var givenPasswordResetToken = passwordResetTokenCreator.createSample(givenUser);
            var resetPasswordRequest = new ResetPasswordRequest(givenPasswordResetToken.getToken(),
                    "1testPassword!");
            //when
            //then
            webTestClient.post()
                    .uri("/password/reset")
                    .bodyValue(resetPasswordRequest)
                    .exchange()
                    .expectStatus().isOk();
            var currentUser = transactionHelper.runInTransactionAndReturn(
                    entityManager -> entityManager.find(User.class, givenUser.getId()));
            Assertions.assertTrue(
                    passwordEncoder.matches(resetPasswordRequest.newPassword(), currentUser.getPassword()));
        }

        @Test
        void givenUserAndCreateCorrectPasswordResetTokenButTokenNotExist_whenResetPassword_thenReturnBadRequest() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            passwordResetTokenCreator.createSample(givenUser);
            var token = passwordResetTokenCreator.generateSecureToken();
            var resetPasswordRequest = new ResetPasswordRequest(token,
                    "1testPassword!");
            //when
            //then
            webTestClient.post()
                    .uri("/password/reset")
                    .bodyValue(resetPasswordRequest)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(InvalidTokenException.class)
                    .returnResult();
        }

        @Test
        void givenUserAndCreateCorrectPasswordResetTokenButIncorrectToken_whenResetPassword_thenReturnBadRequest() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            passwordResetTokenCreator.createSample(givenUser);
            var resetPasswordRequest = new ResetPasswordRequest("token",
                    "1testPassword!");
            //when
            //then
            webTestClient.post()
                    .uri("/password/reset")
                    .bodyValue(resetPasswordRequest)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(InvalidTokenException.class)
                    .returnResult();
        }

        @Test
        void givenUserAndCreateCorrectPasswordResetTokenButIncorrectPassword_whenResetPassword_thenReturnBadRequest() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            var givenPasswordResetToken = passwordResetTokenCreator.createSample(givenUser);
            var resetPasswordRequest = new ResetPasswordRequest(givenPasswordResetToken.getToken(),
                    "test");
            //when
            //then
            webTestClient.post()
                    .uri("/password/reset")
                    .bodyValue(resetPasswordRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenUserAndCreatePasswordResetTokenWithTokenExpired_whenResetPassword_thenReturnBadRequest() {
            //given
            var givenUser = userCreator.createSample("John Doe");
            var givenPasswordResetToken = transactionHelper.runInTransactionAndReturn(entityManager -> {
                var sample = passwordResetTokenCreator.createSample(givenUser);
                sample.setCreatedAt(sample.getCreatedAt().minusHours(1));
                return sample;
            });

            var resetPasswordRequest = new ResetPasswordRequest(givenPasswordResetToken.getToken(),
                    "1testPassword!");
            //when
            //then
            webTestClient.post()
                    .uri("/password/reset")
                    .bodyValue(resetPasswordRequest)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(InvalidTokenException.class)
                    .returnResult();
        }

    }
}