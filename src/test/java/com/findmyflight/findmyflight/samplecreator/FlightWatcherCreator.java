package com.findmyflight.findmyflight.samplecreator;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
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
    public FlightWatcher createSample(boolean suspended, LocalDate from, EmailNotificationReceiver emailNotificationReceiver) {
        var flightWatcher = FlightWatcher.builder()
                .fromCity("gdansk")
                .toCity("rome")
                .fromDate(from)
                .toDate(from.plusDays(1))
                .suspended(suspended)
                .emailNotificationReceiver(emailNotificationReceiver)
                .build();
        return flightWatcherRepository.save(flightWatcher);
    }

    public void deleteAll() {
        flightWatcherRepository.deleteAll();
    }
}