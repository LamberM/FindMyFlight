package com.findmyflight.findmyflight.service.flightwatcher;

import java.time.LocalDate;

public record FlightWatcherResponse(Long id,
                                    String fromCity, String toCity,
                                    LocalDate fromDate, LocalDate toDate,
                                    Boolean suspended) {
}
