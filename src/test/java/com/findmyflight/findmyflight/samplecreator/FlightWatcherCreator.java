package com.findmyflight.findmyflight.samplecreator;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverRepository;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcher;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FlightWatcherCreator {
    private final FlightWatcherRepository flightWatcherRepository;
    private final EmailNotificationReceiverCreator emailNotificationReceiverCreator;
    private final EmailNotificationReceiverRepository emailNotificationReceiverRepository;

    @Transactional
    public FlightWatcher createSample(boolean suspended) {
        var flightWatcher = FlightWatcher.builder()
                .fromCity("gdansk")
                .toCity("rome")
                .fromDate(LocalDate.now())
                .toDate(LocalDate.now().plusDays(1))
                .suspended(suspended)
                .emailNotificationReceiver(emailNotificationReceiverCreator.createSample())
                .build();
        return flightWatcherRepository.save(flightWatcher);
    }
    @Transactional
    public FlightWatcher createSampleWithSameMail(boolean suspended) {
        var flightWatcher = FlightWatcher.builder()
                .fromCity("gdansk")
                .toCity("rome")
                .fromDate(LocalDate.now())
                .toDate(LocalDate.now().plusDays(1))
                .suspended(suspended)
                .emailNotificationReceiver(emailNotificationReceiverRepository.findEmailNotificationReceiverByAddress("test@test.com"))
                .build();
        return flightWatcherRepository.save(flightWatcher);
    }
    public void deleteAll() {
        flightWatcherRepository.deleteAll();
    }
}
