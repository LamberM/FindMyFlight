package com.findmyflight.findmyflight.data.scrape;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("dev")
public class DataScrapeController {
    private final FlightsDataScraper flightsDataScraper;

    @PostMapping("/scrap")
    public ResponseEntity<Void> scrap() {
        flightsDataScraper.scrap();
        return ResponseEntity.ok().build();
    }
}
