package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import com.findmyflight.findmyflight.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collection;


class EmailNotificationReceiverControllerTest implements IntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    EmailNotificationReceiverRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Nested
    class CreateTest {
        @Test
        void shouldCreate() {
            //given
            var emailNotificationReceiverDto = new CreateOrUpdateEmailNotificationReceiverRequest("test@gmail.com");
            //when
            //then
            webTestClient.post()
                    .uri("/emailNotificationReceiver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(EmailNotificationReceiverResponse.class);
        }

        @Test
        void addressIsWrongShouldNotCreate() {
            //given
            var emailNotificationReceiverDto = new CreateOrUpdateEmailNotificationReceiverRequest("tests.com");
            //when
            //then
            webTestClient.post()
                    .uri("/emailNotificationReceiver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    class FindAllTest {
        @Test
        void shouldFindAll() {
            //given
            //when
            //then
            webTestClient.get()
                    .uri("/emailNotificationReceiver")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Collection.class);
        }
    }

    @Nested
    class FindByIdTest {
        @Test
        void shouldFindById() {
            //given
            createEmailNotificationReceiver();
            //when
            //then
            webTestClient.get()
                    .uri("/emailNotificationReceiver/{id}", getIdFromEmailNotificationReceiver())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(EmailNotificationReceiverResponse.class);
        }
    }

    @Nested
    class EditTest {
        @Test
        void shouldEdit() {
            //given
            createEmailNotificationReceiver();
            var emailNotificationReceiverDto = new CreateOrUpdateEmailNotificationReceiverRequest("test123@gmail.com");
            //when
            //then
            webTestClient.put()
                    .uri("/emailNotificationReceiver/{id}", getIdFromEmailNotificationReceiver())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(EmailNotificationReceiverResponse.class);
        }

        @Test
        void addressInObjectIsWrongShouldNotEdit() {
            //given
            createEmailNotificationReceiver();
            var emailNotificationReceiverDto = new CreateOrUpdateEmailNotificationReceiverRequest("testgmail.com");
            //when
            //then
            webTestClient.put()
                    .uri("/emailNotificationReceiver/{id}", getIdFromEmailNotificationReceiver())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(emailNotificationReceiverDto)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    class DeleteTest {
        @Test
        void shouldDelete() {
            //given
            createEmailNotificationReceiver();
            //when
            //then
            webTestClient.delete()
                    .uri("/emailNotificationReceiver/{id}", getIdFromEmailNotificationReceiver())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Long.class);
        }
    }

    private void createEmailNotificationReceiver() {
        var emailNotificationReceiverDto = new CreateOrUpdateEmailNotificationReceiverRequest("test@gmail.com");
        webTestClient.post()
                .uri("/emailNotificationReceiver")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailNotificationReceiverDto)
                .exchange();
    }

    private Long getIdFromEmailNotificationReceiver() {
        return repository.findAll().get(0).getId();
    }
}
