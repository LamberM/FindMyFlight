package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.UnitTest;
import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDate;

@Disabled
class BiletyLotniczeFlightSearchPageTest implements UnitTest {
    // TODO this is just a dummy test used to debug actual implementation - to be removed

    @Test
    void searchFlights() {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless=new");

        ChromeDriver driver = new ChromeDriver(options);

        FlightWatcherResponse flightWatcher = FlightWatcherResponse.builder()
                .fromCity("Warszawa")
                .toCity("Berlin")
                .fromDate(LocalDate.now())
                .toDate(LocalDate.now().plusDays(7))
                .build();

        driver.manage().window().maximize();
        driver.get(BiletyLotniczeFlightsReader.BASE_URL);

        FlightJourney flightJourney = new BiletyLotniczeFlightSearchPage(driver)
                .searchFlights(flightWatcher)
                .readJourney();

        Assertions.assertNotNull(flightJourney);
    }
}