package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BiletyLotniczeFlightSearchResultsPage extends BasePage {

    private static final String SEARCHING_BAR_ELEMENT_SELECTOR = "progress-bar > div";
    private static final Duration SEARCHING_BAR_WAIT_TIMEOUT = Duration.ofSeconds(40);
    private static final int OUTWARD_INDEX = 1;
    private static final int RETURN_INDEX = 2;
    private static final int START_INDEX = 1;
    private static final int END_INDEX = 3;
    private final int MAX_FLIGHT_COUNT = 5;
    private static final String TIME_FIELD_XPATH = "//so-fsr-flight-block[%d]//so-fsr-leg-group[%d]//div[%d]//time[1]//span[1]";
    private static final String AIRPORT_FIELD_XPATH = "//so-fsr-flight-block[%d]//so-fsr-leg-group[%d]//div[%d]//time[1]//span[2]";
    private static final String DAY_FIELD_XPATH = "//so-fsr-flight-block[%d]//so-fsr-leg-group[%d]//div[%d]//time[2]/span";
    private static final String INPUT_DATE_XPATH = "//esky-oneway-roundtrip-form//section[2]//fieldset[2]//div[%d]//input";
    private static final String COST_FIELD_XPATH = "//so-fsr-flight-block[%d]//div[1]//div[1]//workspace-price-formatter";
    private final BiletyLotniczeFlightSearchSingleResultPage outwardFlight;
    private final BiletyLotniczeFlightSearchSingleResultPage returnFlight;

    public BiletyLotniczeFlightSearchResultsPage(WebDriver webDriver) {
        super(webDriver);
        outwardFlight = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
        returnFlight = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
    }


    public Collection<FlightJourney> readJourneys() {
        waitUntilNotPresent(By.cssSelector(SEARCHING_BAR_ELEMENT_SELECTOR), SEARCHING_BAR_WAIT_TIMEOUT);
        waitUntilPresent(By.xpath(getCostFieldXpath(1)));
        Set<FlightJourney> flightJourneys = new HashSet<>();
        for (int i = 1; i < MAX_FLIGHT_COUNT + 1; i++) {
            FlightJourney flightJourney = FlightJourney.builder()
                    .outwardFlight(outwardFlight.readFlight(getTimeXPath(i, OUTWARD_INDEX, START_INDEX),
                            getTimeXPath(i, OUTWARD_INDEX, END_INDEX),
                            getAirportTimeXPath(i, OUTWARD_INDEX, START_INDEX),
                            getAirportTimeXPath(i, OUTWARD_INDEX, END_INDEX),
                            getDayXPath(i, OUTWARD_INDEX, START_INDEX),
                            getDayXPath(i, OUTWARD_INDEX, END_INDEX),
                            getInputDateXPath(OUTWARD_INDEX)))
                    .returnFlight(returnFlight.readFlight(getTimeXPath(i, RETURN_INDEX, START_INDEX),
                            getTimeXPath(i, RETURN_INDEX, END_INDEX),
                            getAirportTimeXPath(i, RETURN_INDEX, START_INDEX),
                            getAirportTimeXPath(i, RETURN_INDEX, END_INDEX),
                            getDayXPath(i, RETURN_INDEX, START_INDEX),
                            getDayXPath(i, RETURN_INDEX, END_INDEX),
                            getInputDateXPath(RETURN_INDEX)))
                    .cost(webDriver.findElement(By.xpath(getCostFieldXpath(i))).getText())
                    .build();
            flightJourneys.add(flightJourney);
        }
        return flightJourneys;
    }

    private String getTimeXPath(int countOffer, int typeFlight, int typeTime) {
        return String.format(TIME_FIELD_XPATH, countOffer, typeFlight, typeTime);
    }

    private String getAirportTimeXPath(int countOffer, int typeFlight, int typeTime) {
        return String.format(AIRPORT_FIELD_XPATH, countOffer, typeFlight, typeTime);
    }

    private String getDayXPath(int countOffer, int typeFlight, int typeTime) {
        return String.format(DAY_FIELD_XPATH, countOffer, typeFlight, typeTime);
    }

    private String getInputDateXPath(int typeFlight) {
        return String.format(INPUT_DATE_XPATH, typeFlight);
    }

    private String getCostFieldXpath(int countOffer) {
        return String.format(COST_FIELD_XPATH, countOffer);
    }
}
