package com.findmyflight.findmyflight.service.emailnotificationreceiver;


import com.findmyflight.findmyflight.UnitTest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EmailNotificationReceiverServiceTest implements UnitTest {

    private static final Long SAMPLE_ID = 1L;
    @InjectMocks
    private EmailNotificationReceiverService systemUnderTest;
    @Mock
    private EmailNotificationReceiverRepository repositoryMock;
    @Mock
    private EmailNotificationReceiverMapper mapperMock;
    @Mock
    private EmailNotificationReceiver emailNotificationReceiverMock;
    @Mock
    private CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequestMock;

    @Nested
    class CreateTest {
        @Test
        void shouldCreate() {
            //given
            Mockito.when(mapperMock.map(createOrUpdateEmailNotificationReceiverRequestMock)).thenReturn(emailNotificationReceiverMock);
            //when
            var result = systemUnderTest.create(createOrUpdateEmailNotificationReceiverRequestMock);
            //then
            Mockito.verify(mapperMock).map(createOrUpdateEmailNotificationReceiverRequestMock);
            Mockito.verify(repositoryMock).save(emailNotificationReceiverMock);
            Assertions.assertEquals(mapperMock.map(emailNotificationReceiverMock), result);
        }

    }

    @Nested
    class FindAllTest {
        @Test
        void shouldFindAll() {
            //given
            Mockito.when(repositoryMock.findAll()).thenReturn(new ArrayList<>());
            //when
            var result = systemUnderTest.findAll();
            //then
            Mockito.verify(repositoryMock).findAll();
            Assertions.assertEquals(mapperMock.map(repositoryMock.findAll()), result);
        }
    }

    @Nested
    class FindByIdTest {
        @Test
        void shouldFindById() {
            //given
            Mockito.when(repositoryMock.findById(SAMPLE_ID)).thenReturn(Optional.ofNullable(emailNotificationReceiverMock));
            //when
            var result = systemUnderTest.findById(SAMPLE_ID);
            //then
            Mockito.verify(repositoryMock).findById(SAMPLE_ID);
            Assertions.assertEquals(mapperMock.map(emailNotificationReceiverMock), result);
        }

        @Test
        void dbIsEmptyShouldNotFindById() {
            //given
            //when
            //then
            assertThatThrownBy(() -> systemUnderTest.findById(SAMPLE_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class EditTest {

        @Test
        void shouldEdit() {
            //given
            Mockito.when(repositoryMock.findById(SAMPLE_ID)).thenReturn(Optional.ofNullable(emailNotificationReceiverMock));
            Mockito.when(repositoryMock.save(emailNotificationReceiverMock)).thenReturn(emailNotificationReceiverMock);
            //when
            var result = systemUnderTest.update(SAMPLE_ID, createOrUpdateEmailNotificationReceiverRequestMock);
            //then
            Mockito.verify(repositoryMock).findById(SAMPLE_ID);
            Mockito.verify(mapperMock).updateFromRequest(createOrUpdateEmailNotificationReceiverRequestMock, emailNotificationReceiverMock);
            Mockito.verify(repositoryMock).save(emailNotificationReceiverMock);
            Assertions.assertEquals(mapperMock.map(emailNotificationReceiverMock), result);
        }

        @Test
        void addressNotExistShouldNotEdit() {
            //given
            //when
            //then
            assertThatThrownBy(() -> systemUnderTest.update(SAMPLE_ID, createOrUpdateEmailNotificationReceiverRequestMock))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class DeleteTest {

        @Test
        void shouldDelete() {
            //given
            //when
            systemUnderTest.delete(SAMPLE_ID);
            //then
            Mockito.verify(repositoryMock).deleteById(SAMPLE_ID);
        }
    }
}