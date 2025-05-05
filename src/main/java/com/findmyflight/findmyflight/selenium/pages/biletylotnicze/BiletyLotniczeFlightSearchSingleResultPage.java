package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.data.scrape.SingleFlight;
import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class BiletyLotniczeFlightSearchSingleResultPage extends BasePage {

    private static final String COST_FIELD_XPATH = "//flights-list/div/div[2]/div/div/ul/li[1]/esky-offer-group-container/div/div[1]/div/div[2]/esky-offer-price-info-container/div/div[1]/div/esky-offer-price-info/div/esky-price";

    private WebElement costField;

    public BiletyLotniczeFlightSearchSingleResultPage(WebDriver webDriver) {
        super(webDriver);
    }

    public SingleFlight readFlight(String startTimeXpath, String endTimeXpath, String startAirportXpath, String endAirportXpath, String startDayXpath, String endDayXpath) {
        costField = webDriver.findElement(By.xpath(COST_FIELD_XPATH));
        var flightStartDetails = new BiletyLotniczeFlightDetailsPage(webDriver, startTimeXpath, startAirportXpath, startDayXpath);
        var flightEndDetails = new BiletyLotniczeFlightDetailsPage(webDriver, endTimeXpath, endAirportXpath, endDayXpath);
        return SingleFlight.builder()
                .referenceUrl(webDriver.getCurrentUrl())
                .journey(List.of(
                        flightStartDetails.parseToJourneyPoint(),
                        flightEndDetails.parseToJourneyPoint()
                ))
                .cost(costField.getText())
                .build();
    }
}
