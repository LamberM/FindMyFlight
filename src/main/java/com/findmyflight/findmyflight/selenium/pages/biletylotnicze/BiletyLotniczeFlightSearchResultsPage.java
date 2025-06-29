package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;

public class BiletyLotniczeFlightSearchResultsPage extends BasePage {

    private static final String SEARCHING_BAR_ELEMENT_SELECTOR = "progress-bar > div";
    private static final Duration SEARCHING_BAR_WAIT_TIMEOUT = Duration.ofSeconds(40);
    private static final int OUTWARD_INDEX = 1;
    private static final int RETURN_INDEX = 2;
    private static final int START_INDEX = 1;
    private static final int END_INDEX = 3;
    private static final String TIME_FIELD_XPATH = "//so-fsr-leg-group[%d]//div[%d]//time[1]//span[1]";
    private static final String AIRPORT_FIELD_XPATH = "//so-fsr-leg-group[%d]//div[%d]//time[1]//span[2]";
    private static final String DAY_FIELD_XPATH = "//so-fsr-leg-group[%d]//div[%d]//time[2]/span";
    private static final String INPUT_DATE_XPATH = "//esky-oneway-roundtrip-form//section[2]//fieldset[2]//div[%d]//input";
    private static final String COST_FIELD_XPATH = "//so-fsr-flight-block[1]//div[1]//div[1]//workspace-price-formatter";
    private final BiletyLotniczeFlightSearchSingleResultPage outwardFlight;
    private final BiletyLotniczeFlightSearchSingleResultPage returnFlight;
    private WebElement costField;

    public BiletyLotniczeFlightSearchResultsPage(WebDriver webDriver) {
        super(webDriver);
        outwardFlight = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
        returnFlight = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
    }

    public FlightJourney readJourney() {
        waitUntilNotPresent(By.cssSelector(SEARCHING_BAR_ELEMENT_SELECTOR), SEARCHING_BAR_WAIT_TIMEOUT);
        waitUntilPresent(By.xpath(COST_FIELD_XPATH));
        costField = webDriver.findElement(By.xpath(COST_FIELD_XPATH));
        return FlightJourney.builder()
                .outwardFlight(outwardFlight.readFlight(getTimeXPath(OUTWARD_INDEX, START_INDEX), getTimeXPath(OUTWARD_INDEX, END_INDEX),
                        getAirportTimeXPath(OUTWARD_INDEX, START_INDEX), getAirportTimeXPath(OUTWARD_INDEX, END_INDEX),
                        getDayXPath(OUTWARD_INDEX, START_INDEX), getDayXPath(OUTWARD_INDEX, END_INDEX), getInputDateXPath(OUTWARD_INDEX)))
                .returnFlight(returnFlight.readFlight(getTimeXPath(RETURN_INDEX, START_INDEX), getTimeXPath(RETURN_INDEX, END_INDEX),
                        getAirportTimeXPath(RETURN_INDEX, START_INDEX), getAirportTimeXPath(RETURN_INDEX, END_INDEX),
                        getDayXPath(RETURN_INDEX, START_INDEX), getDayXPath(RETURN_INDEX, END_INDEX), getInputDateXPath(RETURN_INDEX)))
                .cost(costField.getText())
                .build();
    }

    private String getTimeXPath(int typeFlight, int typeTime) {
        return String.format(TIME_FIELD_XPATH, typeFlight, typeTime);
    }

    private String getAirportTimeXPath(int typeFlight, int typeTime) {
        return String.format(AIRPORT_FIELD_XPATH, typeFlight, typeTime);
    }

    private String getDayXPath(int typeFlight, int typeTime) {
        return String.format(DAY_FIELD_XPATH, typeFlight, typeTime);
    }

    private String getInputDateXPath(int typeFlight) {
        return String.format(INPUT_DATE_XPATH, typeFlight);
    }
}
