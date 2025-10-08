package com.findmyflight.findmyflight.data.scrape;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FlightJourney(SingleFlight outwardFlight, SingleFlight returnFlight, BigDecimal cost) {
}
