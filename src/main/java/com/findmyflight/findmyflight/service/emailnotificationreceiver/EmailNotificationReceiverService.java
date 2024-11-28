package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailNotificationReceiverService {
    private final EmailNotificationReceiverRepository emailNotificationReceiverRepository;
    private final EmailNotificationReceiverMapper emailNotificationReceiverMapper;

    @Transactional
    public EmailNotificationReceiverResponse create(CreateOrUpdateEmailNotificationReceiverRequest request) {
        var emailNotificationReceiver = emailNotificationReceiverMapper.map(request);
        emailNotificationReceiverRepository.save(emailNotificationReceiver);
        return emailNotificationReceiverMapper.map(emailNotificationReceiver);
    }

    @Transactional(readOnly = true)
    public Collection<EmailNotificationReceiverResponse> findAll() {
        return emailNotificationReceiverMapper.map(emailNotificationReceiverRepository.findAll());
    }

    @Transactional(readOnly = true)
    public EmailNotificationReceiverResponse findById(Long id) {
        return emailNotificationReceiverMapper.map(emailNotificationReceiverRepository.findById(id).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public Optional<EmailNotificationReceiver> findEntityById(Long id) {
        return emailNotificationReceiverRepository.findById(id);
    }

    @Transactional
    public EmailNotificationReceiverResponse update(Long id, CreateOrUpdateEmailNotificationReceiverRequest request) {
        var emailNotificationReceiver = emailNotificationReceiverRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        emailNotificationReceiverMapper.updateFromRequest(request, emailNotificationReceiver);
        emailNotificationReceiverRepository.save(emailNotificationReceiver);
        return emailNotificationReceiverMapper.map(emailNotificationReceiver);
    }

    @Transactional
    public void delete(Long id) {
        emailNotificationReceiverRepository.deleteById(id);
    }
}
