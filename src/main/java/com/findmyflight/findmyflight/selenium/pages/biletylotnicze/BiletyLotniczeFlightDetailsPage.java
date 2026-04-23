package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.JourneyPoint;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BiletyLotniczeFlightDetailsPage extends BasePage {
    private static final String LEG_DEPARTURE_SELECTOR = "[data-testid='leg-departure']";
    private static final String LEG_ARRIVAL_SELECTOR = "[data-testid='leg-arrival']";
    private static final String TIME_FIELD_SELECTOR = "[data-testid='time']";
    private static final String DATE_FIELD_SELECTOR = "[data-testid='date']";
    private static final String AIRPORT_FIELD_SELECTOR = "[data-testid='airport-code']";

    public BiletyLotniczeFlightDetailsPage(WebDriver webDriver) {
        super(webDriver);
    }

    public JourneyPoint getJourneyPoint(WebElement legGroup, FlightLegType flightLegType, LocalDate flightDate) {
        var departure = legGroup.findElement(By.cssSelector(LEG_DEPARTURE_SELECTOR));
        var arrival = legGroup.findElement(By.cssSelector(LEG_ARRIVAL_SELECTOR));
        waitUntilDisplayed(departure, arrival);
        return switch (flightLegType) {
            case DEPARTURE -> readJourneyPoint(departure, flightDate);
            case ARRIVAL -> readJourneyPoint(arrival, flightDate);
            case null, default -> JourneyPoint.builder().build();
        };
    }

    private JourneyPoint readJourneyPoint(WebElement typeOfFlight, LocalDate flightDate) {
        var airport = typeOfFlight.findElement(By.cssSelector(AIRPORT_FIELD_SELECTOR));
        var date = typeOfFlight.findElement(By.cssSelector(DATE_FIELD_SELECTOR));
        var time = typeOfFlight.findElement(By.cssSelector(TIME_FIELD_SELECTOR));
        return JourneyPoint.builder()
                .airport(airport.getText())
                .dateTime(LocalDateTime.of(getLocalDate(date, flightDate), getLocalTime(time)))
                .build();
    }

    private LocalTime getLocalTime(WebElement time) {
        return LocalTime.parse(time.getText());
    }

    private LocalDate getLocalDate(WebElement date, LocalDate flightDate) {
        var dateString = date.getText();
        String[] parts = dateString.split(" ");
        var day = parts[0];
        var polishMonth = parts[1];
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
        var parsed = LocalDate.from(
                formatter.parse(flightDate.getYear() + "-" + getMonthInNumber(polishMonth) + "-" + day));
        return parsed.isBefore(flightDate) ? parsed.plusYears(1) : parsed;
    }

    private String getMonthInNumber(String month) {
        return switch (month) {
            case "sty" -> "01";
            case "lut" -> "02";
            case "mar" -> "03";
            case "kwi" -> "04";
            case "maj" -> "05";
            case "cze" -> "06";
            case "lip" -> "07";
            case "sie" -> "08";
            case "wrz" -> "09";
            case "paź" -> "10";
            case "lis" -> "11";
            case "gru" -> "12";
            default -> "error";
        };
    }

}
