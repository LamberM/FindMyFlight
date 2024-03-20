package com.findmyflight.findmyflight.service.emailnotificationreceiver;


import com.findmyflight.findmyflight.UnitTest;
import com.findmyflight.findmyflight.service.error.handler.AddressExistException;
import com.findmyflight.findmyflight.service.error.handler.IdNotExistException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EmailNotificationReceiverServiceTest implements UnitTest {

    @InjectMocks
    EmailNotificationReceiverService systemUnderTest;

    @Mock
    EmailNotificationReceiverRepository repositoryMock;

    @Mock
    EmailNotificationReceiverMapper mapperMock;

    @Mock
    EmailNotificationReceiver emailNotificationReceiverMock;

    @Mock
    EmailNotificationReceiverDto emailNotificationReceiverDtoMock;

    @Nested
    class CreateEmailNotificationReceiverTest{
        @Test
        void shouldCreateEmailNotificationReceiver(){
            //given
            Mockito.when(repositoryMock.existsByAddress(emailNotificationReceiverDtoMock.address())).thenReturn(false);
            Mockito.when(mapperMock.map(emailNotificationReceiverDtoMock)).thenReturn(emailNotificationReceiverMock);
            //when
            systemUnderTest.createEmailNotificationReceiver(emailNotificationReceiverDtoMock);
            //then
            Mockito.verify(mapperMock).map(emailNotificationReceiverDtoMock);
            Mockito.verify(repositoryMock).save(emailNotificationReceiverMock);
        }
        @Test
        void emailExistsShouldNotCreateEmailNotificationReceiver(){
            //given
            Mockito.when(repositoryMock.existsByAddress(emailNotificationReceiverDtoMock.address())).thenReturn(true);
            //when
            //then
            assertThatThrownBy(()->systemUnderTest.createEmailNotificationReceiver(emailNotificationReceiverDtoMock))
                    .isInstanceOf(AddressExistException.class)
                    .hasMessage("Address exists");
        }
    }
    @Nested
    class GetAllEmailNotificationReceiversTest{
        @Test
        void shouldGetAllEmailNotificationReceivers(){
            //given
            Mockito.when(repositoryMock.findAll()).thenReturn(new ArrayList<>());
            //when
            systemUnderTest.getAllEmailNotificationReceivers();
            //then
            Mockito.verify(repositoryMock).findAll();
        }
    }
    @Nested
    class EditEmailNotificationReceiverTest{
        Long id = 1L;
        @Test
        void shouldEditEmailNotificationReceiver(){
            //given
            Mockito.when(repositoryMock.existsById(id)).thenReturn(true);
            Mockito.when(repositoryMock.findById(id)).thenReturn(Optional.ofNullable(emailNotificationReceiverMock));
            Mockito.when(repositoryMock.save(emailNotificationReceiverMock)).thenReturn(emailNotificationReceiverMock);
            //when
            systemUnderTest.editEmailNotificationReceiver(id,emailNotificationReceiverDtoMock);
            //then
            Mockito.verify(repositoryMock).existsById(id);
            Mockito.verify(repositoryMock).findById(id);
            Mockito.verify(mapperMock).updateEmailNotificationReceiverFromDto(emailNotificationReceiverDtoMock,emailNotificationReceiverMock);
            Mockito.verify(repositoryMock).save(emailNotificationReceiverMock);
        }
        @Test
        void addressNotExistShouldNotEditEmailNotificationReceiver(){
            //given
            Mockito.when(repositoryMock.existsById(id)).thenReturn(false);
            //when
            //then
            assertThatThrownBy(()->systemUnderTest.editEmailNotificationReceiver(id,emailNotificationReceiverDtoMock))
                    .isInstanceOf(IdNotExistException.class)
                    .hasMessage("ID does not exist");
        }
    }
    @Nested
    class DeleteEmailNotificationReceiverTest{
        Long id = 1L;
        @Test
        void shouldDeleteEmailNotificationReceiver(){
            //given
            Mockito.when(repositoryMock.existsById(id)).thenReturn(true);
            //when
            systemUnderTest.deleteEmailNotificationReceiver(id);
            //then
            Mockito.verify(repositoryMock).deleteById(id);
        }
        @Test
        void idNotExistShouldNotDeleteEmailNotificationReceiver(){
            //given
            Mockito.when(repositoryMock.existsById(id)).thenReturn(false);
            //when
            //then
            assertThatThrownBy(()->systemUnderTest.deleteEmailNotificationReceiver(id))
                    .isInstanceOf(IdNotExistException.class)
                    .hasMessage("ID does not exist");
        }
    }
}