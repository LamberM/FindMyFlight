package com.findmyflight.findmyflight.samplecreator;

import com.findmyflight.findmyflight.service.flightresult.FlightResult;
import com.findmyflight.findmyflight.service.flightresult.FlightResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FlightResultCreator {
    private final FlightResultRepository flightResultRepository;
    private final FlightWatcherCreator flightWatcherCreator;

    @Transactional
    public FlightResult createSample(boolean flightWatcherSuspended) {
        var flightResult = FlightResult.builder()
                .result("1000")
                .flightWatcher(flightWatcherCreator.createSample(flightWatcherSuspended, LocalDate.of(2025,1,1)))
                .build();
        return flightResultRepository.save(flightResult);
    }

    @Transactional
    public void deleteAll() {
        flightResultRepository.deleteAll();
    }
}
