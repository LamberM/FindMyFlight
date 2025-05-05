package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class BiletyLotniczeFlightSearchResultsPage extends BasePage {

    public static final String SEARCHING_BAR_ELEMENT_SELECTOR = "progress-bar > div";
    public static final Duration SEARCHING_BAR_WAIT_TIMEOUT = Duration.ofSeconds(40);
    private static final int OUTWARD_INDEX = 1;
    private static final int RETURN_INDEX = 2;
    private static final int START_INDEX = 1;
    private static final int END_INDEX = 3;
    private static final String TIME_XPATH = "//flights-list/div/div[2]/div/div/ul/li[1]/esky-offer-group-container/div/div[1]/div/div[1]/div/esky-offer[%d]/div/div[1]/div[%d]/div[1]/span[1]";
    private static final String AIRPORT_XPATH = "//flights-list/div/div[2]/div/div/ul/li[1]/esky-offer-group-container/div/div[1]/div/div[1]/div/esky-offer[%d]/div/div[1]/div[%d]/div[1]/span[2]";
    private static final String DAY_XPATH = "//flights-list/div/div[2]/div/div/ul/li[1]/esky-offer-group-container/div/div[1]/div/div[1]/div/esky-offer[%d]/div/div[1]/div[%d]/div[2]/span";

    private final BiletyLotniczeFlightSearchSingleResultPage outwardFlight;
    private final BiletyLotniczeFlightSearchSingleResultPage returnFlight;

    public BiletyLotniczeFlightSearchResultsPage(WebDriver webDriver) {
        super(webDriver);
        outwardFlight = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
        returnFlight = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
    }

    public FlightJourney readJourney() {
        waitUntilNotPresent(By.cssSelector(SEARCHING_BAR_ELEMENT_SELECTOR), SEARCHING_BAR_WAIT_TIMEOUT);
        return FlightJourney.builder()
                .outwardFlight(outwardFlight.readFlight(getTimeXPath(OUTWARD_INDEX, START_INDEX), getTimeXPath(OUTWARD_INDEX, END_INDEX),
                        getAirportTimeXPath(OUTWARD_INDEX, START_INDEX), getAirportTimeXPath(OUTWARD_INDEX, END_INDEX),
                        getDayXPath(OUTWARD_INDEX, START_INDEX), getDayXPath(OUTWARD_INDEX, END_INDEX)))
                .returnFlight(returnFlight.readFlight(getTimeXPath(RETURN_INDEX, START_INDEX), getTimeXPath(RETURN_INDEX, END_INDEX),
                        getAirportTimeXPath(RETURN_INDEX, START_INDEX), getAirportTimeXPath(RETURN_INDEX, END_INDEX),
                        getDayXPath(RETURN_INDEX, START_INDEX), getDayXPath(RETURN_INDEX, END_INDEX)))
                .build();
    }

    private String getTimeXPath(int typeFlight, int typeTime) {
        return String.format(TIME_XPATH, typeFlight, typeTime);
    }

    private String getAirportTimeXPath(int typeFlight, int typeTime) {
        return String.format(AIRPORT_XPATH, typeFlight, typeTime);
    }

    private String getDayXPath(int typeFlight, int typeTime) {
        return String.format(DAY_XPATH, typeFlight, typeTime);
    }
}
