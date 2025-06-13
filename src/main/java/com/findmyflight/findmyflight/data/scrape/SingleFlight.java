package com.findmyflight.findmyflight.data.scrape;

import lombok.Builder;

import java.util.List;

@Builder
public record SingleFlight(String referenceUrl, List<JourneyPoint> journey) {
}
