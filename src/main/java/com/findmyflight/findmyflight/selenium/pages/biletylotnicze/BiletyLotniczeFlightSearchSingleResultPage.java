package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.SingleFlight;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class BiletyLotniczeFlightSearchSingleResultPage extends BasePage {

    public BiletyLotniczeFlightSearchSingleResultPage(WebDriver webDriver) {
        super(webDriver);
    }

    public SingleFlight readFlight(String startTimeXPath, String endTimeXPath, String startAirportXPath, String endAirportXPath, String startDayXPath, String endDayXPath, String inputDateXPath) {
        var flightStartDetails = new BiletyLotniczeFlightDetailsPage(webDriver, startTimeXPath, startAirportXPath, startDayXPath, inputDateXPath);
        var flightEndDetails = new BiletyLotniczeFlightDetailsPage(webDriver, endTimeXPath, endAirportXPath, endDayXPath, inputDateXPath);
        return SingleFlight.builder()
                .referenceUrl(webDriver.getCurrentUrl())
                .journey(List.of(
                        flightStartDetails.parseToJourneyPoint(),
                        flightEndDetails.parseToJourneyPoint()
                ))
                .build();
    }
}
