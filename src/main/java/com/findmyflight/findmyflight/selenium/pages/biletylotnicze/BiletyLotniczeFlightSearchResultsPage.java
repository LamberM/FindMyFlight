package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.FlightJourney;
import com.findmyflight.findmyflight.data.scrape.Price;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BiletyLotniczeFlightSearchResultsPage extends BasePage {

    private static final String SEARCHING_BAR_ELEMENT_SELECTOR = "progress-bar > div";
    private static final Duration SEARCHING_BAR_WAIT_TIMEOUT = Duration.ofSeconds(40);
    private static final String MAX_FLIGHT_FROM_SITE_ELEMENT_SELECTOR = "p.results";
    private static final Duration MAX_FLIGHT_FROM_SITE_WAIT_TIMEOUT = Duration.ofSeconds(20);
    private static final String SHOW_MORE_BUTTON_SELECTOR = "[data-testid='more-results-button']";
    private static final int DEFAULT_MAX_FLIGHT_COUNT = 5;
    private static final String FLIGHT_BLOCK_SELECTOR = "[data-testid='flight-block']";
    private static final String COST_FIELD_SELECTOR = ".amount.notranslate";
    private static final String CURRENCY_FIELD_SELECTOR = ".currency.notranslate";

    private final BiletyLotniczeFlightSearchSingleResultPage singleResultPage;

    public BiletyLotniczeFlightSearchResultsPage(WebDriver webDriver) {
        super(webDriver);
        singleResultPage = new BiletyLotniczeFlightSearchSingleResultPage(webDriver);
    }

    public Collection<FlightJourney> readJourneys(Integer maxFlightFilter, BigDecimal maxPriceFilter,
                                                  LocalDate fromDate, LocalDate toDate) {
        waitUntilNotPresent(By.cssSelector(SEARCHING_BAR_ELEMENT_SELECTOR), SEARCHING_BAR_WAIT_TIMEOUT);
        waitUntilPresent(By.cssSelector(MAX_FLIGHT_FROM_SITE_ELEMENT_SELECTOR),
                Optional.ofNullable(MAX_FLIGHT_FROM_SITE_WAIT_TIMEOUT));
        var maxFlight = maxFlightFilter != null && maxFlightFilter != 0 ? maxFlightFilter : DEFAULT_MAX_FLIGHT_COUNT;
        Set<FlightJourney> flightJourneys = new HashSet<>();
        int processedIndex = 0;

        while (flightJourneys.size() < maxFlight) {
            List<WebElement> flightBlocks = webDriver.findElements(By.cssSelector(FLIGHT_BLOCK_SELECTOR));

            if (processedIndex >= flightBlocks.size()) {
                List<WebElement> showMoreButtons = webDriver.findElements(By.cssSelector(SHOW_MORE_BUTTON_SELECTOR));
                if (!showMoreButtons.isEmpty() && showMoreButtons.getFirst().isDisplayed()) {
                    int countBeforeClick = flightBlocks.size();
                    showMoreButtons.getFirst().click();
                    waitUntil(d -> d.findElements(By.cssSelector(FLIGHT_BLOCK_SELECTOR)).size() > countBeforeClick,
                            Duration.ofSeconds(10));
                } else {
                    break;
                }
                continue;
            }

            var flightBlock = flightBlocks.get(processedIndex++);
            new Actions(webDriver).scrollToElement(flightBlock).perform();
            var flightJourney = readFlightJourney(flightBlock, fromDate, toDate);
            if (maxPriceFilter.compareTo(flightJourney.price().cost()) >= 0) {
                flightJourneys.add(flightJourney);
            }
        }
        return flightJourneys;
    }

    private FlightJourney readFlightJourney(WebElement flightBlock, LocalDate fromDate, LocalDate toDate) {
        return FlightJourney.builder()
                .outwardFlight(singleResultPage.getSingleFlight(flightBlock, FlightDirection.OUTWARD, fromDate))
                .returnFlight(singleResultPage.getSingleFlight(flightBlock, FlightDirection.RETURN, toDate))
                .price(readPrice(flightBlock))
                .build();
    }

    private Price readPrice(WebElement flightBlock) {
        var cost = flightBlock.findElement(By.cssSelector(COST_FIELD_SELECTOR)).getText().trim();
        var currency = flightBlock.findElement(By.cssSelector(CURRENCY_FIELD_SELECTOR)).getText().trim();

        var cleanCost = cost.replaceAll("[^0-9]", "");
        return Price.builder()
                .cost(new BigDecimal(cleanCost))
                .currency(currency)
                .build();
    }
}
