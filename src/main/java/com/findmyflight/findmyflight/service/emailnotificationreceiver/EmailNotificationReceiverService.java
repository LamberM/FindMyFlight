package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EmailNotificationReceiverService {
    private final EmailNotificationReceiverRepository repository;
    private final EmailNotificationReceiverMapper mapper;

    @Transactional
    public EmailNotificationReceiverResponse create(CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest) {
        var emailNotificationReceiver = mapper.map(createOrUpdateEmailNotificationReceiverRequest);
        repository.save(emailNotificationReceiver);
        return mapper.map(emailNotificationReceiver);
    }

    @Transactional(readOnly = true)
    public Collection<EmailNotificationReceiverResponse> findAll() {
        return mapper.map(repository.findAll());
    }

    @Transactional(readOnly = true)
    public EmailNotificationReceiverResponse findById(Long id) {
        return mapper.map(repository.findById(id).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional
    public EmailNotificationReceiverResponse edit(Long id, CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest) {
        var emailNotificationReceiver = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        mapper.updateFromRequest(createOrUpdateEmailNotificationReceiverRequest, emailNotificationReceiver);
        repository.save(emailNotificationReceiver);
        return mapper.map(emailNotificationReceiver);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

}
