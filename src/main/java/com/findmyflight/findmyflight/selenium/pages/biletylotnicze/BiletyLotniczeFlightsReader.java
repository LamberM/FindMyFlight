package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.selenium.pages.FlightsReader;
import com.findmyflight.findmyflight.selenium.pages.WebDriverFactory;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BiletyLotniczeFlightsReader implements FlightsReader {

    public static final String BASE_URL = "https://biletylotnicze.pl";

    private final WebDriverFactory webDriverFactory;

    @Override
    public Map<FlightWatcherResponse, Collection<FlightJourney>> read(
            Collection<FlightWatcherResponse> flightWatchers) {
        var driver = webDriverFactory.get();
        var searchPage = new BiletyLotniczeFlightSearchPage(driver);

        try {
            var result = new HashMap<FlightWatcherResponse, Collection<FlightJourney>>();
            for (FlightWatcherResponse flightWatcherResponse : flightWatchers) {
                driver.get(BASE_URL);
                var flightJourneys = searchPage.searchFlights(flightWatcherResponse).readJourneys();
                result.put(flightWatcherResponse, flightJourneys);
            }
            return result;
        } finally {
            driver.quit();
        }
    }
}
