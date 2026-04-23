package com.findmyflight.findmyflight.data.scrape;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Price(BigDecimal cost, String currency) {
}
