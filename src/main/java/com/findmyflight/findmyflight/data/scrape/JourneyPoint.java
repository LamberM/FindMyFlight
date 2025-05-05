package com.findmyflight.findmyflight.data.scrape;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record JourneyPoint(String airport, LocalDateTime dateTime) {

    public static final JourneyPoint SOLD_OUT = new JourneyPoint("SOLD OUT", LocalDateTime.now());
}
