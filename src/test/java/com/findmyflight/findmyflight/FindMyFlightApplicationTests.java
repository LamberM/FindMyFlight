package com.findmyflight.findmyflight;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverDto;
import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
class FindMyFlightApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    EmailNotificationReceiverRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Nested
    class EmailNotificationReceiverTest {
        @Test
        void shouldCreateEmailNotificationReceiver() {
            //given
            var emailNotificationReceiverDto = new EmailNotificationReceiverDto("test@gmail.com");
            //when
            //then
            webTestClient.post()
                    .uri("/emailNotificationReceiver/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(EmailNotificationReceiver.class);
        }

        @Test
        void addressIsWrongShouldNotCreateEmailNotificationReceiver() {
            //given
            var emailNotificationReceiverDto = new EmailNotificationReceiverDto("tests.com");
            //when
            //then
            webTestClient.post()
                    .uri("/emailNotificationReceiver/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void shouldGetAllEmailNotificationReceivers() {
            //given
            //when
            //then
            webTestClient.get()
                    .uri("/emailNotificationReceiver/get/all")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(List.class);
        }

        @Test
        void shouldEditEmailNotificationReceiver() {
            //given
            createEmailNotificationReceiver();
            var emailNotificationReceiverDto = new EmailNotificationReceiverDto("test123@gmail.com");
            //when
            //then
            webTestClient.put()
                    .uri("/emailNotificationReceiver/update/{id}", getIdFromEmailNotificationReceiver())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        void addressInObjectIsWrongShouldNotEditEmailNotificationReceiver() {
            //given
            createEmailNotificationReceiver();
            var emailNotificationReceiverDto = new EmailNotificationReceiverDto("testgmail.com");
            //when
            //then
            webTestClient.put()
                    .uri("/emailNotificationReceiver/update/{id}", getIdFromEmailNotificationReceiver())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void shouldDeleteEmailNotificationReceiver() {
            //given
            createEmailNotificationReceiver();
            //when
            //then
            webTestClient.delete()
                    .uri("/emailNotificationReceiver/delete/{id}", getIdFromEmailNotificationReceiver())
                    .exchange()
                    .expectStatus().isOk();
        }

        private void createEmailNotificationReceiver() {
            var emailNotificationReceiverDto = new EmailNotificationReceiverDto("test@gmail.com");
            webTestClient.post()
                    .uri("/emailNotificationReceiver/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange();
        }

        private Long getIdFromEmailNotificationReceiver() {
            return repository.findAll().get(0).getId();
        }
    }
}
