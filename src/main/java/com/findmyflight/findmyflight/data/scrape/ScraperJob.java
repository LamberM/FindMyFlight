package com.findmyflight.findmyflight.data.scrape;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScraperJob {

    private final FlightsDataScraper flightsDataScraper;

    @Scheduled(cron = "${app.scraping-cron}")
    public void runScraping() {
        log.info("running scheduled scraping...");
        flightsDataScraper.scrap();
        log.info("scraping complete");
    }
}
