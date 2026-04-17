package com.findmyflight.findmyflight.service.user;

import com.findmyflight.findmyflight.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collection;

class UserControllerIT extends IntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        userCreator.deleteAll();
    }

    @Nested
    class CreateIT {
        @Test
        void givenValidRequest_thenCreate() {
            //given
            var request = CreateUserRequest.builder()
                    .login("test@gmail.com")
                    .fullName("John Doe")
                    .password("4testPassword!")
                    .build();
            //when
            //then
            var response = webTestClient.post()
                    .uri("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserResponse.class)
                    .returnResult()
                    .getResponseBody();

            Assertions.assertThat(response).isNotNull();

            transactionHelper.runInTransaction(entityManager -> {
                var user = entityManager.find(User.class, response.id());
                Assertions.assertThat(user)
                        .isNotNull()
                        .returns(request.login(), User::getLogin)
                        .returns(request.fullName(), User::getFullName);
                Assertions.assertThat(passwordEncoder.matches(request.password(),user.getPassword())).isTrue();
            });
        }

        @Test
        void givenInvalidPasswordRequest_thenBadRequest() {
            //given
            var request = CreateUserRequest.builder()
                    .login("test1@gmail.com")
                    .password("testPassword!")
                    .fullName("John Doe")
                    .build();
            //when
            //then
            webTestClient.post()
                    .uri("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .returnResult(MethodArgumentNotValidException.class);
        }

        @Test
        void givenInvalidLoginRequest_thenBadRequest() {
            //given
            var request = CreateUserRequest.builder()
                    .login("test2")
                    .password("3testPassword!")
                    .fullName("John Doe")
                    .build();
            //when
            //then
            webTestClient.post()
                    .uri("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .returnResult(MethodArgumentNotValidException.class);
        }
    }

    @Nested
    class FindAllIT{
        @Test
        void givenUsers_thenReturnAll() {
            //given
            var sample1 = userCreator.createSample("Jane Doe");
            var sample2 = userCreator.createSample("John Smith");
            var sample3 = userCreator.createSample("John Doe");
            //when
            //then
            var response = webTestClient.get()
                    .uri("/user")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<Collection<UserResponse>>() {
                    })
                    .returnResult()
                    .getResponseBody();
            Assertions.assertThat(response)
                    .containsExactly(
                            userMapper.map(sample1),
                            userMapper.map(sample2),
                            userMapper.map(sample3)
                    );
        }

    }

    @Nested
    class FindByIdIT {
        @Test
        void givenUser_thenReturnById() {
            //given
            var sample = userCreator.createSample("John Doe");
            //when
            //then
            webTestClient.get()
                    .uri("/user/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(sample.getId())
                    .jsonPath("$.login").isEqualTo(sample.getLogin())
                    .jsonPath("$.fullName").isEqualTo(sample.getFullName())
                    .jsonPath("$.createdAt").isEqualTo(sample.getCreatedAt())
                    .jsonPath("$.updatedAt").isEqualTo(sample.getUpdatedAt())
                    .jsonPath("$.lastLoginAt").isEqualTo(sample.getLastLoginAt());
        }

    }

    @Nested
    class UpdateIT {
        @Test
        void givenSampleAndRequest_thenUpdate() {
            //given
            var sample = userCreator.createSample("John Doe");
            var request = UpdateUserRequest.builder()
                    .fullName("Johnny Doe")
                    .build();
            //when
            //then
            webTestClient.put()
                    .uri("/user/{id}", sample.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserResponse.class)
                    .returnResult();

            transactionHelper.runInTransaction(entityManager -> {
                var user = entityManager.find(User.class, sample.getId());
                Assertions.assertThat(user)
                        .isNotNull()
                        .returns(request.fullName(), User::getFullName);
            });
        }
    }

    @Nested
    class DeleteIT{
        @Test
        void givenUser_thenDelete() {
            //given
            var sample = userCreator.createSample("John Doe");
            //when
            //then
            webTestClient.delete()
                    .uri("/user/{id}", sample.getId())
                    .exchange()
                    .expectStatus().isOk();
            transactionHelper.runInTransaction(entityManager -> {
                var user = entityManager.find(User.class, sample.getId());
                Assertions.assertThat(user).isNull();
            });
        }
    }
}