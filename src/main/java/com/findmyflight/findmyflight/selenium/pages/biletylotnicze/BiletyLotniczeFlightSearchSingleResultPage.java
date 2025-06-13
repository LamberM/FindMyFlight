package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.SingleFlight;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class BiletyLotniczeFlightSearchSingleResultPage extends BasePage {

    public BiletyLotniczeFlightSearchSingleResultPage(WebDriver webDriver) {
        super(webDriver);
    }

    public SingleFlight readFlight(String startTimeXpath, String endTimeXpath, String startAirportXpath, String endAirportXpath, String startDayXpath, String endDayXpath) {
        var flightStartDetails = new BiletyLotniczeFlightDetailsPage(webDriver, startTimeXpath, startAirportXpath, startDayXpath);
        var flightEndDetails = new BiletyLotniczeFlightDetailsPage(webDriver, endTimeXpath, endAirportXpath, endDayXpath);
        return SingleFlight.builder()
                .referenceUrl(webDriver.getCurrentUrl())
                .journey(List.of(
                        flightStartDetails.parseToJourneyPoint(),
                        flightEndDetails.parseToJourneyPoint()
                ))
                .build();
    }
}
