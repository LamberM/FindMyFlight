package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BiletyLotniczeFlightSearchResultsPage extends BasePage {

    private static final String SEARCHING_BAR_ELEMENT_SELECTOR = "progress-bar > div";
    private static final Duration SEARCHING_BAR_WAIT_TIMEOUT = Duration.ofSeconds(40);
    private static final String MAX_FLIGHT_FROM_SITE_ELEMENT_SELECTOR = "p.results";
    private static final Duration MAX_FLIGHT_FROM_SITE_WAIT_TIMEOUT = Duration.ofSeconds(20);
    private static final String SHOW_MORE_BUTTON_SELECTOR = "ecs-button.load-more-btn";
    private static final int DEFAULT_MAX_FLIGHT_COUNT = 5;
    private static final int OUTWARD_INDEX = 1;
    private static final int RETURN_INDEX = 2;
    private static final int START_INDEX = 1;
    private static final int END_INDEX = 3;
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


    public Collection<FlightJourney> readJourneys(Integer maxFlightFilter, BigDecimal maxPriceFilter) {
        waitUntilNotPresent(By.cssSelector(SEARCHING_BAR_ELEMENT_SELECTOR), SEARCHING_BAR_WAIT_TIMEOUT);
        waitUntilPresent(By.cssSelector(MAX_FLIGHT_FROM_SITE_ELEMENT_SELECTOR), MAX_FLIGHT_FROM_SITE_WAIT_TIMEOUT);
        Set<FlightJourney> flightJourneys = new HashSet<>();
        var i = 1;
        var maxFlight = maxFlightFilter != null && maxFlightFilter != 0 ? maxFlightFilter : DEFAULT_MAX_FLIGHT_COUNT;
        var maxFlightFromSite = getMaxFlightFromSite();
        while (flightJourneys.size() != maxFlight || i < maxFlightFromSite) {
            if (i % 20 == 0) {
                var showMoreButton = webDriver.findElement(By.cssSelector(SHOW_MORE_BUTTON_SELECTOR));
                showMoreButton.click();
            }
            waitUntilPresent(By.xpath(getCostFieldXpath(i)), Duration.ofSeconds(10));
            new Actions(webDriver)
                    .scrollToElement(webDriver.findElement(By.xpath(getCostFieldXpath(i))))
                    .perform();
            var cost = getDigitCost(webDriver.findElement(By.xpath(getCostFieldXpath(i))).getText());
            if (maxPriceFilter.compareTo(cost) >= 0) {
                var flightJourney = FlightJourney.builder()
                        .outwardFlight(outwardFlight.readFlight(getTimeXPath(i, OUTWARD_INDEX, START_INDEX),
                                getTimeXPath(i, OUTWARD_INDEX, END_INDEX),
                                getAirportTimeXPath(i, OUTWARD_INDEX, START_INDEX),
                                getAirportTimeXPath(i, OUTWARD_INDEX, END_INDEX),
                                getDayXPath(i, OUTWARD_INDEX, START_INDEX),
                                getDayXPath(i, OUTWARD_INDEX, END_INDEX),
                                getInputDateXPath(i)))
                        .returnFlight(returnFlight.readFlight(getTimeXPath(i, RETURN_INDEX, START_INDEX),
                                getTimeXPath(i, RETURN_INDEX, END_INDEX),
                                getAirportTimeXPath(i, RETURN_INDEX, START_INDEX),
                                getAirportTimeXPath(i, RETURN_INDEX, END_INDEX),
                                getDayXPath(i, RETURN_INDEX, START_INDEX),
                                getDayXPath(i, RETURN_INDEX, END_INDEX),
                                getInputDateXPath(i)))
                        .cost(cost)
                        .build();
                flightJourneys.add(flightJourney);
            }
            i++;
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

    private Integer getMaxFlightFromSite() {
        var maxFlightWithLetters = webDriver.findElement(By.cssSelector(MAX_FLIGHT_FROM_SITE_ELEMENT_SELECTOR))
                .getText();
        var maxFlight = maxFlightWithLetters.replaceAll("[^0-9]", "");
        return Integer.valueOf(maxFlight);
    }

    private BigDecimal getDigitCost(String cost) {
        var digitCost = cost.replaceAll("[^0-9]", "");
        return new BigDecimal(digitCost);
    }
}
