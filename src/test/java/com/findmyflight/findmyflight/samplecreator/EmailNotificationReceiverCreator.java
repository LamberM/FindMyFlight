package com.findmyflight.findmyflight.samplecreator;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverRepository;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class EmailNotificationReceiverCreator {
    private static final Random RANDOM = new Random();
    private static final String EMAIL_SAMPLE_PATTERN = "test-email-%d@test.com";
    private final EmailNotificationReceiverRepository emailNotificationReceiverRepository;

    @Transactional
    public EmailNotificationReceiver createSample() {
        var emailNotificationReceiver = EmailNotificationReceiver.builder().address(generateEmail()).build();
        return emailNotificationReceiverRepository.save(emailNotificationReceiver);
    }
    @Transactional
    public EmailNotificationReceiver createSample(List<FlightWatcher> flightWatcherList) {
        var emailNotificationReceiver = EmailNotificationReceiver.builder()
                .address(generateEmail())
                .flightWatchers(flightWatcherList)
                .build();
        return emailNotificationReceiverRepository.save(emailNotificationReceiver);
    }

    @Transactional
    public void deleteAll() {
        emailNotificationReceiverRepository.deleteAll();
    }

    private String generateEmail() {
        return String.format(EMAIL_SAMPLE_PATTERN, RANDOM.nextInt());
    }
}
