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
    private final WebElement flightHour;
    private final WebElement flightAirport;
    private final WebElement flightDate;
    private final WebElement flightInputDate;

    private static final String INPUT_DATE_ATTRIBUTE_NAME = "data-qa-value";

    public BiletyLotniczeFlightDetailsPage(WebDriver webDriver, String timeXPath, String airportXPath, String dayXPath, String inputDateXPath) {
        super(webDriver);
        flightHour = webDriver.findElement(By.xpath(timeXPath));
        flightAirport = webDriver.findElement(By.xpath(airportXPath));
        flightDate = webDriver.findElement(By.xpath(dayXPath));
        flightInputDate = webDriver.findElement(By.xpath(inputDateXPath));
    }

    public JourneyPoint parseToJourneyPoint() {
        waitUntilDisplayed(flightHour, flightAirport, flightDate);

        return JourneyPoint.builder()
                .airport(flightAirport.getText())
                .dateTime(LocalDateTime.of(getLocalDate(), getLocalTime()))
                .build();
    }

    private LocalTime getLocalTime() {
        return LocalTime.parse(flightHour.getText());
    }

    private LocalDate getLocalDate() {
        var dateString = flightDate.getText();
        String[] parts = dateString.split(" ");
        var day = parts[0];
        var polishMonth = parts[1];
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
        var temporalAccessor = formatter.parse(getYear() + "-" + getMonthInNumber(polishMonth) + "-" + day);
        return LocalDate.from(temporalAccessor);
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

    private String getYear() {
        var inputDateString = flightInputDate.getAttribute(INPUT_DATE_ATTRIBUTE_NAME);
        var inputDate = LocalDate.parse(inputDateString);
        return String.valueOf(inputDate.getYear());
    }
}
