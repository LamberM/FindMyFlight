package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import com.findmyflight.findmyflight.service.error.handler.AddressExistException;
import com.findmyflight.findmyflight.service.error.handler.IdNotExistException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailNotificationReceiverService {
    private final EmailNotificationReceiverRepository repository;
    private final EmailNotificationReceiverMapper mapper;

    @Transactional
    public void createEmailNotificationReceiver(EmailNotificationReceiverDto emailNotificationReceiverDto) {
        if (repository.existsByAddress(emailNotificationReceiverDto.address())) {
            throw new AddressExistException();
        } else {
            var emailNotificationReceiver = mapper.map(emailNotificationReceiverDto);
            repository.save(emailNotificationReceiver);
        }
    }

    @Transactional(readOnly = true)
    public List<EmailNotificationReceiver> getAllEmailNotificationReceivers() {
        return repository.findAll();
    }

    @Transactional
    public void editEmailNotificationReceiver(Long id, EmailNotificationReceiverDto emailNotificationReceiverDto) {
        repository.findById(id)
                .ifPresentOrElse(emailNotificationReceiver -> {
                            mapper.updateEmailNotificationReceiverFromDto(emailNotificationReceiverDto,
                                    emailNotificationReceiver);
                            repository.save(emailNotificationReceiver);
                        },
                        () -> {
                            throw new EntityNotFoundException();
                        }
                );
    }

    @Transactional
    public void deleteEmailNotificationReceiver(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new IdNotExistException();
        }
    }

}
