package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import com.findmyflight.findmyflight.IntegrationTest;
import com.findmyflight.findmyflight.samplecreator.EmailNotificationReceiverCreator;
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
    EmailNotificationReceiverCreator creator;

    @AfterEach
    void cleanUp() {
        creator.deleteAll();
    }

    private CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest(String address) {
        return new CreateOrUpdateEmailNotificationReceiverRequest(address);
    }

    @Nested
    class CreateTest {
        @Test
        void shouldCreate() {
            //given
            CreateOrUpdateEmailNotificationReceiverRequest request = createOrUpdateEmailNotificationReceiverRequest("test@gmail.com");
            //when
            //then
            webTestClient.post()
                    .uri("/email-notification-receiver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.address").isEqualTo("test@gmail.com");
        }

        @Test
        void addressIsWrongShouldNotCreate() {
            //given
            CreateOrUpdateEmailNotificationReceiverRequest request = createOrUpdateEmailNotificationReceiverRequest("tests.com");
            //when
            //then
            webTestClient.post()
                    .uri("/email-notification-receiver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    class FindAllTest {
        @Test
        void shouldFindAll() {
            //given
            EmailNotificationReceiver sample = creator.createSample();
            //when
            //then
            webTestClient.get()
                    .uri("/email-notification-receiver")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[0].id").isEqualTo(sample.getId())
                    .jsonPath("$[0].address").isEqualTo(sample.getAddress());
        }

        @Test
        void shouldFindAllCollectionEmpty() {
            //given
            //when
            //then
            webTestClient.get()
                    .uri("/email-notification-receiver")
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
            EmailNotificationReceiver sample = creator.createSample();
            //when
            //then
            webTestClient.get()
                    .uri("/email-notification-receiver/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.address").isEqualTo(sample.getAddress());
        }
    }

    @Nested
    class EditTest {
        @Test
        void shouldEdit() {
            //given
            EmailNotificationReceiver sample = creator.createSample();
            //when
            //then
            webTestClient.put()
                    .uri("/email-notification-receiver/{id}", sample.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createOrUpdateEmailNotificationReceiverRequest("test123@gmail.com"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.address").isEqualTo("test123@gmail.com");
        }
    }

    @Nested
    class DeleteTest {
        @Test
        void shouldDelete() {
            //given
            EmailNotificationReceiver sample = creator.createSample();
            //when
            //then
            webTestClient.delete()
                    .uri("/email-notification-receiver/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk();
        }
    }
}
