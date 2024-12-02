package com.findmyflight.findmyflight.service.flightwatcher;

import com.findmyflight.findmyflight.IntegrationTest;
import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

class FlightWatcherControllerTest extends IntegrationTest {

    @Autowired
    private FlightWatcherMapper flightWatcherMapper;

    @AfterEach
    void cleanUp() {
        flightWatcherCreator.deleteAll();
    }

    @Nested
    class FindAllTest {
        @Test
        void givenFlightWatchers_thenReturnAll() {
            // given
            var sample1 = flightWatcherCreator.createSample();
            var sample2 = flightWatcherCreator.createSample();
            var sample3 = flightWatcherCreator.createSample();
            // when
            // then
            var response = webTestClient.get()
                    .uri("/flight-watcher")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<Collection<FlightWatcherResponse>>() {
                    })
                    .returnResult()
                    .getResponseBody();

            Assertions.assertThat(response)
                    .containsExactly(
                            flightWatcherMapper.map(sample1),
                            flightWatcherMapper.map(sample2),
                            flightWatcherMapper.map(sample3)
                    );
        }
    }

    @Nested
    class FindByIdTest {
        @Test
        void givenFlightWatcher_thenReturn() {
            // given
            var sample = flightWatcherCreator.createSample();
            // when
            // then
            webTestClient.get()
                    .uri("/flight-watcher/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(sample.getId())
                    .jsonPath("$.fromCity").isEqualTo(sample.getFromCity())
                    .jsonPath("$.toCity").isEqualTo(sample.getToCity())
                    .jsonPath("$.fromDate").isEqualTo(sample.getFromDate().format(DateTimeFormatter.ISO_DATE))
                    .jsonPath("$.toDate").isEqualTo(sample.getToDate().format(DateTimeFormatter.ISO_DATE))
                    .jsonPath("$.suspended").isEqualTo(sample.getSuspended());
        }
    }

    @Nested
    class CreateTest {
        @Test
        void givenValidRequest_thenCreate() {
            //given
            var sampleReceiver = emailNotificationReceiverCreator.createSample();
            var request = CreateOrUpdateFlightWatcherRequest.builder()
                    .fromCity("gdansk")
                    .toCity("rome")
                    .fromDate(LocalDate.now())
                    .toDate(LocalDate.now().plusDays(1))
                    .suspended(Boolean.FALSE)
                    .emailNotificationReceiverId(sampleReceiver.getId())
                    .build();
            //when
            //then
            var response = webTestClient.post()
                    .uri("/flight-watcher")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(FlightWatcherResponse.class)
                    .returnResult()
                    .getResponseBody();

            Assertions.assertThat(response).isNotNull();

            transactionHelper.runInTransaction(entityManager -> {
                var flightWatcher = entityManager.find(FlightWatcher.class, response.id());
                Assertions.assertThat(flightWatcher)
                        .isNotNull()
                        .returns(request.fromCity(), FlightWatcher::getFromCity)
                        .returns(request.toCity(), FlightWatcher::getToCity)
                        .returns(request.fromDate(), FlightWatcher::getFromDate)
                        .returns(request.toDate(), FlightWatcher::getToDate)
                        .returns(request.suspended(), FlightWatcher::getSuspended);
                Assertions.assertThat(flightWatcher)
                        .extracting(FlightWatcher::getEmailNotificationReceiver)
                        .extracting(EmailNotificationReceiver::getId)
                        .isEqualTo(sampleReceiver.getId());
            });
        }
    }

    @Nested
    class UpdateTest {
        @Test
        void givenValidRequestAndEntityExists_thenUpdate() {
            //given
            var sample = flightWatcherCreator.createSample();
            var request = CreateOrUpdateFlightWatcherRequest.builder()
                    .fromCity(sample.getFromCity())
                    .toCity(sample.getToCity())
                    .fromDate(sample.getFromDate())
                    .toDate(sample.getToDate().plusDays(1))
                    .suspended(sample.getSuspended())
                    .emailNotificationReceiverId(sample.getEmailNotificationReceiver().getId())
                    .build();
            //when
            //then
            webTestClient.put()
                    .uri("/flight-watcher/{id}", sample.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk();

            transactionHelper.runInTransaction(entityManager -> {
                var flightWatcher = entityManager.find(FlightWatcher.class, sample.getId());
                Assertions.assertThat(flightWatcher)
                        .isNotNull()
                        .returns(request.fromCity(), FlightWatcher::getFromCity)
                        .returns(request.toCity(), FlightWatcher::getToCity)
                        .returns(request.fromDate(), FlightWatcher::getFromDate)
                        .returns(request.toDate(), FlightWatcher::getToDate)
                        .returns(request.suspended(), FlightWatcher::getSuspended);
            });
        }
    }

    @Nested
    class DeleteTest {
        @Test
        void givenEntityExists_thenDelete() {
            //given
            var sample = flightWatcherCreator.createSample();
            //when
            //then
            webTestClient.delete()
                    .uri("/flight-watcher/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk();

            transactionHelper.runInTransaction(entityManager -> {
                var flightWatcher = entityManager.find(FlightWatcher.class, sample.getId());
                Assertions.assertThat(flightWatcher).isNull();
            });
        }
    }

    @Nested
    class SuspendResumeTest {
        @Test
        void givenWatcherResumed_whenSuspend_thenSuspendWatcher() {
            //given
            var resumedWatcher = transactionHelper.runInTransactionAndReturn(entityManager -> {
                var sample = flightWatcherCreator.createSample();
                sample.setSuspended(Boolean.FALSE);
                return sample;
            });
            //when
            //then
            webTestClient.put()
                    .uri("/flight-watcher/{id}/suspend", resumedWatcher.getId())
                    .exchange()
                    .expectStatus().isOk();

            transactionHelper.runInTransaction(entityManager -> {
                var flightWatcher = entityManager.find(FlightWatcher.class, resumedWatcher.getId());
                Assertions.assertThat(flightWatcher)
                        .isNotNull()
                        .returns(Boolean.TRUE, FlightWatcher::getSuspended);
            });
        }

        @Test
        void givenWatcherSuspended_whenResume_thenResumeWatcher() {
            //given
            var suspendedWatcher = transactionHelper.runInTransactionAndReturn(entityManager -> {
                var sample = flightWatcherCreator.createSample();
                sample.setSuspended(Boolean.TRUE);
                return sample;
            });
            //when
            //then
            webTestClient.put()
                    .uri("/flight-watcher/{id}/resume", suspendedWatcher.getId())
                    .exchange()
                    .expectStatus().isOk();

            transactionHelper.runInTransaction(entityManager -> {
                var flightWatcher = entityManager.find(FlightWatcher.class, suspendedWatcher.getId());
                Assertions.assertThat(flightWatcher)
                        .isNotNull()
                        .returns(Boolean.FALSE, FlightWatcher::getSuspended);
            });
        }
    }
}