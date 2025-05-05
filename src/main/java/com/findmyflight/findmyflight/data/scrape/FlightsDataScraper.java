package com.findmyflight.findmyflight.data.scrape;

import com.findmyflight.findmyflight.selenium.pages.FlightsReader;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherResponse;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FlightsDataScraper {

    private final List<FlightsReader> flightsReaders;
    private final FlightWatcherService flightWatcherService;

    public void scrap() {
        Collection<FlightWatcherResponse> allWatchers = flightWatcherService.findAll();
        for (FlightsReader flightsReader : flightsReaders) {
            flightsReader.read(allWatchers);
        }
    }
}
