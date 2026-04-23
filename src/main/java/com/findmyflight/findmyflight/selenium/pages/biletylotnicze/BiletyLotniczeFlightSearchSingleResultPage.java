package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.SingleFlight;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;

public class BiletyLotniczeFlightSearchSingleResultPage extends BasePage {
    private static final String LEG_GROUP_SELECTOR = "[data-testid='leg-group']";

    private final BiletyLotniczeFlightDetailsPage flightDetails;

    public BiletyLotniczeFlightSearchSingleResultPage(WebDriver webDriver) {
        super(webDriver);
        flightDetails = new BiletyLotniczeFlightDetailsPage(webDriver);
    }

    public SingleFlight getSingleFlight(WebElement flightBlock, FlightDirection flightDirection, LocalDate date) {
        List<WebElement> legGroups = flightBlock.findElements(By.cssSelector(LEG_GROUP_SELECTOR));
        return switch (flightDirection) {
            case OUTWARD -> readSingleFlight(legGroups.get(0), date);
            case RETURN -> readSingleFlight(legGroups.get(1), date);
            case null, default -> SingleFlight.builder().build();
        };
    }

    private SingleFlight readSingleFlight(WebElement legGroup, LocalDate date) {
        return SingleFlight.builder()
                .referenceUrl(webDriver.getCurrentUrl())
                .journey(List.of(
                        flightDetails.getJourneyPoint(legGroup, FlightLegType.DEPARTURE, date),
                        flightDetails.getJourneyPoint(legGroup, FlightLegType.ARRIVAL, date)
                ))
                .build();
    }
}
