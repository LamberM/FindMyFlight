package com.findmyflight.findmyflight.selenium.pages;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherResponse;

import java.util.Collection;
import java.util.Map;

public interface FlightsReader {

    Map<FlightWatcherResponse, Collection<FlightJourney>> read(Collection<FlightWatcherResponse> flightWatchers);
}
