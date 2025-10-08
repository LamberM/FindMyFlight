package com.findmyflight.findmyflight.service.flightwatcher;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record FlightWatcherResponse(Long id,
                                    String fromCity, String toCity,
                                    LocalDate fromDate, LocalDate toDate,
                                    Boolean suspended, Integer maxFlightCount,
                                    @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal maxPrice) {
}
