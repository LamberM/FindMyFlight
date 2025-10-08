package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.selenium.pages.BasePage;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherResponse;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Optional;

@Slf4j
public class BiletyLotniczeFlightSearchPage extends BasePage {

    private static final String CITY_TO_PICK_TABLE_ELEMENT_SELECTOR = ".SBCSS-dropdown:not(.SBCSS-hidden)";
    private static final String CITY_TO_PICK_SELECTOR_PATTERN = ".SBCSS-dropdown-subrow[data-suggestion*='%s']";
    private static final String FROM_MONTH_FIELD_SELECTOR = ".SBCSS-calendar-container-start.SBCSS-flights > div > div";
    private static final String FROM_RIGHT_ARROW_FIELD_SELECTOR = "div.SBCSS-calendar-container-start.SBCSS-flights.SBCSS-open > div > i.SBCSS-icon-arrow-right-bold.SBCSS-right";
    private static final String TO_RIGHT_ARROW_FIELD_SELECTOR = " div.SBCSS-calendar-container-end.SBCSS-flights.SBCSS-open > div > i.SBCSS-icon-arrow-right-bold.SBCSS-right";
    private static final String TO_MONTH_FIELD_SELECTOR = ".SBCSS-calendar-container-end.SBCSS-flights > div > div";

    @FindBy(id = "tr_0_d")
    private WebElement fromCityField;
    @FindBy(id = "tr_0_a")
    private WebElement toCityField;
    @FindBy(id = "tr_0_dd")
    private WebElement fromDateField;
    @FindBy(id = "tr_1_dd")
    private WebElement toDateField;
    @FindBy(css = "[type=\"submit\"]")
    private WebElement submitButton;

    public BiletyLotniczeFlightSearchPage(WebDriver webDriver) {
        super(webDriver);
    }

    public BiletyLotniczeFlightSearchResultsPage searchFlights(FlightWatcherResponse flightWatcher) {
        PageFactory.initElements(webDriver, this);

        waitUntilDisplayed(fromCityField, toCityField, fromDateField, toDateField, submitButton);
        pickCity(fromCityField, flightWatcher.fromCity());
        pickCity(toCityField, flightWatcher.toCity());
        new BiletyLotniczeDatePicker(webDriver, fromDateField, FROM_MONTH_FIELD_SELECTOR,
                FROM_RIGHT_ARROW_FIELD_SELECTOR).pickDate(flightWatcher.fromDate());
        new BiletyLotniczeDatePicker(webDriver, toDateField, TO_MONTH_FIELD_SELECTOR,
                TO_RIGHT_ARROW_FIELD_SELECTOR).pickDate(flightWatcher.toDate());
        submitButton.click();
        return new BiletyLotniczeFlightSearchResultsPage(webDriver);
    }

    private void pickCity(WebElement cityInputElement, String city) {
        cityInputElement.sendKeys(city);
        waitUntilPresent(By.cssSelector(CITY_TO_PICK_TABLE_ELEMENT_SELECTOR), Optional.empty());

        WebElement targetCityTableElement = webDriver.findElement(
                By.cssSelector(CITY_TO_PICK_SELECTOR_PATTERN.formatted(city)));
        waitUntilDisplayed(targetCityTableElement);

        targetCityTableElement.click();
        waitUntilDisappear(targetCityTableElement);
    }
}
