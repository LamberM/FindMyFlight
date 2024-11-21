package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import com.findmyflight.findmyflight.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


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
            //when
            //then
            webTestClient.post()
                    .uri("/emailNotificationReceiver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createOrUpdateEmailNotificationReceiverRequest("test@gmail.com"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.address").isEqualTo("test@gmail.com");
        }

        @Test
        void addressIsWrongShouldNotCreate() {
            //given
            //when
            //then
            webTestClient.post()
                    .uri("/emailNotificationReceiver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createOrUpdateEmailNotificationReceiverRequest("tests.com"))
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    class FindAllTest {
        @Test
        void shouldFindAll() {
            //given
            emailNotificationReceiver();
            //when
            //then
            webTestClient.get()
                    .uri("/emailNotificationReceiver")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[0].id").isEqualTo(emailNotificationReceiver().getId())
                    .jsonPath("$[0].address").isEqualTo(emailNotificationReceiver().getAddress());
        }
        @Test
        void shouldFindAllCollectionEmpty() {
            //given
            //when
            //then
            webTestClient.get()
                    .uri("/emailNotificationReceiver")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").doesNotExist()
                    .jsonPath("$.address").doesNotExist();
        }
    }

    @Nested
    class FindByIdTest {
        @Test
        void shouldFindById() {
            //given
            emailNotificationReceiver();
            //when
            //then
            webTestClient.get()
                    .uri("/emailNotificationReceiver/{id}", emailNotificationReceiver().getId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.address").isEqualTo("test@gmail.com");
        }
    }

    @Nested
    class EditTest {
        @Test
        void shouldEdit() {
            //given
            emailNotificationReceiver();
            //when
            //then
            webTestClient.put()
                    .uri("/emailNotificationReceiver/{id}", emailNotificationReceiver().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createOrUpdateEmailNotificationReceiverRequest("test123@gmail.com"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.address").isEqualTo("test123@gmail.com");
        }

        @Test
        void addressInObjectIsWrongShouldNotEdit() {
            //given
            //when
            //then
            webTestClient.put()
                    .uri("/emailNotificationReceiver/{id}", emailNotificationReceiver().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createOrUpdateEmailNotificationReceiverRequest("testgmail.com"))
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    class DeleteTest {
        @Test
        void shouldDelete() {
            //given
            //when
            //then
            webTestClient.delete()
                    .uri("/emailNotificationReceiver/{id}", emailNotificationReceiver().getId())
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    private CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest(String address) {
        return new CreateOrUpdateEmailNotificationReceiverRequest(address);
    }

    private EmailNotificationReceiver emailNotificationReceiver() {
        var emailNotificationReceiver = new EmailNotificationReceiver(1L, "test@gmail.com");
        return repository.save(emailNotificationReceiver);
    }
}
