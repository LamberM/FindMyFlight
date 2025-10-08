package com.findmyflight.findmyflight.service.flightwatcher;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CreateOrUpdateFlightWatcherRequest(
        @NotBlank(message = "fromCity should not be blank") String fromCity,
        @NotBlank(message = "toCity should not be blank") String toCity,
        LocalDate fromDate,
        LocalDate toDate,
        @NotNull Boolean suspended,
        @DecimalMin(value = "1", message = "maxPrice should be more than 0") BigDecimal maxPrice,
        @NotNull @Min(1) Integer maxFlightCount,
        @NotNull Long emailNotificationReceiverId
) {
}
