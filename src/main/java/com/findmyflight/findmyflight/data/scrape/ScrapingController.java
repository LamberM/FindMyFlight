package com.findmyflight.findmyflight.data.scrape;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scrap")
@Validated
@RequiredArgsConstructor
public class ScrapingController {

    private final FlightsDataScraper flightsDataScraper;

    @PostMapping
    public ResponseEntity<Void> scrap() {
        flightsDataScraper.scrap();
        return ResponseEntity.ok().build();
    }
}
