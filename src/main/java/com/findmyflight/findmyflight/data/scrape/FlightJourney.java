package com.findmyflight.findmyflight.data.scrape;

import lombok.Builder;

@Builder
public record FlightJourney(SingleFlight outwardFlight, SingleFlight returnFlight, Price price) {
}
