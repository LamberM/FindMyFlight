package com.findmyflight.findmyflight.service.flightwatcher;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record FlightWatcherResponse(Long id,
                                    String fromCity, String toCity,
                                    LocalDate fromDate, LocalDate toDate,
                                    Boolean suspended) {
}
