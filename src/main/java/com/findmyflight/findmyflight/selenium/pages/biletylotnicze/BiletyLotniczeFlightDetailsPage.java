package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.JourneyPoint;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BiletyLotniczeFlightDetailsPage extends BasePage {
    private final WebElement flightHour;
    private final WebElement flightAirport;
    private final WebElement flightDate;

    public BiletyLotniczeFlightDetailsPage(WebDriver webDriver, String timeXPath, String airportXPath, String dayXPath) {
        super(webDriver);
        flightHour = webDriver.findElement(By.xpath(timeXPath));
        flightAirport = webDriver.findElement(By.xpath(airportXPath));
        flightDate = webDriver.findElement(By.xpath(dayXPath));
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
        var stringBuilder = new StringBuilder(flightDate.getText());
        var year = 0;
        var month = 0;
        var day = 0;
        if (dayIsDigit(stringBuilder)) {
            day = Integer.parseInt(stringBuilder.substring(0, 1));
            month = getMonth(stringBuilder.substring(2, 5));
            year = getYear(month);
            return LocalDate.of(year, month, day);
        } else {
            day = Integer.parseInt(stringBuilder.substring(0, 2));
            month = getMonth(stringBuilder.substring(3, 6));
            year = getYear(month);
            return LocalDate.of(year, month, day);
        }
    }

    private int getMonth(String month) {
        return switch (month) {
            case "sty" -> 1;
            case "lut" -> 2;
            case "mar" -> 3;
            case "kwi" -> 4;
            case "maj" -> 5;
            case "cze" -> 6;
            case "lip" -> 7;
            case "sie" -> 8;
            case "wrz" -> 9;
            case "paź" -> 10;
            case "lis" -> 11;
            case "gru" -> 12;
            default -> 0;
        };
    }

    private int getYear(int month) {
        if (month >= LocalDate.now().getMonthValue()) {
            return LocalDate.now().getYear();
        } else {
            return LocalDate.now().getYear() + 1;
        }
    }

    private static boolean dayIsDigit(StringBuilder stringBuilder) {
        return stringBuilder.charAt(1) == ' ';
    }
}
