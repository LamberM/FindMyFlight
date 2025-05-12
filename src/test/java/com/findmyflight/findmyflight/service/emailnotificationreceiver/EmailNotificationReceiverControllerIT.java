package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import com.findmyflight.findmyflight.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class EmailNotificationReceiverControllerIT extends IntegrationTest {

    @AfterEach
    void cleanUp() {
        emailNotificationReceiverCreator.deleteAll();
    }

    private CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest(String address) {
        return new CreateOrUpdateEmailNotificationReceiverRequest(address);
    }

    @Nested
    class CreateTest {
        @Test
        void shouldCreate() {
            //given
            var request = createOrUpdateEmailNotificationReceiverRequest("test@gmail.com");
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
            var request = createOrUpdateEmailNotificationReceiverRequest("tests.com");
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
            var sample = emailNotificationReceiverCreator.createSample();
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
            var sample = emailNotificationReceiverCreator.createSample();
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
            var sample = emailNotificationReceiverCreator.createSample();
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
            var sample = emailNotificationReceiverCreator.createSample();
            //when
            //then
            webTestClient.delete()
                    .uri("/email-notification-receiver/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk();
        }
    }
}
