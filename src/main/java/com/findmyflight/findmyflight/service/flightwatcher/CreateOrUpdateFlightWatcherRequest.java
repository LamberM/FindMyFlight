package com.findmyflight.findmyflight.service.flightwatcher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateOrUpdateFlightWatcherRequest(
        @NotBlank(message = "fromCity should not be blank") String fromCity,
        @NotBlank(message = "toCity should not be blank") String toCity,
        LocalDate fromDate,
        LocalDate toDate,
        @NotNull Boolean suspended,
        @NotNull Long emailNotificationReceiverId
) {
}
