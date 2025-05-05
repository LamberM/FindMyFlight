package com.findmyflight.findmyflight.selenium.pages.biletylotnicze;

import com.findmyflight.findmyflight.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class BiletyLotniczeDatePicker extends BasePage {

    private static final String AVAILABLE_DAYS_SELECTOR = ".SBCSS-calendar > tbody > tr > td.SBCSS-active";
    public static final String MONTH_ATTRIBUTE = "qa-month";
    private final WebElement dateField;
    private WebElement rightArrow;

    public BiletyLotniczeDatePicker(WebDriver webDriver, WebElement dateField) {
        super(webDriver);
        this.dateField = dateField;
    }

    public void pickDate(LocalDate dateToPick, String monthSelector, String rightArrowSelector) {
        dateField.click();
        waitUntilPresent(By.cssSelector(monthSelector));
        var monthValue = webDriver.findElement(By.cssSelector(monthSelector));
        findDate(Integer.parseInt(monthValue.getAttribute(MONTH_ATTRIBUTE)), dateToPick, rightArrowSelector);
    }

    private void findDate(int monthValue, LocalDate dateToPick, String rightArrowSelector) {
        var monthToPick = convertDateTo(dateToPick);
        var dayToPick = dateToPick.getDayOfMonth();
        if (monthToPick - monthValue < 0) {
            var nextYearPick = 12 + monthToPick - monthValue;
            for (int i = 0; i < nextYearPick; i++) {
                waitUntil(driver -> driver.findElement(By.cssSelector(rightArrowSelector)).isDisplayed(), Duration.ofSeconds(2));
                rightArrow = webDriver.findElement(By.cssSelector(rightArrowSelector));
                rightArrow.click();
            }
        } else {
            for (int i = 0; i < monthToPick - monthValue; i++) {
                waitUntil(driver -> driver.findElement(By.cssSelector(rightArrowSelector)).isDisplayed(), Duration.ofSeconds(2));
                rightArrow = webDriver.findElement(By.cssSelector(rightArrowSelector));
                rightArrow.click();
            }
        }
        List<WebElement> calendarDayElements = webDriver.findElements(By.cssSelector(AVAILABLE_DAYS_SELECTOR));
        for (WebElement element : calendarDayElements) {
            if (!element.getText().isEmpty() && Integer.parseInt(element.getText()) == dayToPick) {
                element.click();
                waitUntilDisappear(element);
            }
        }
    }

    private int convertDateTo(LocalDate dateToPick) {
        return dateToPick.getMonthValue() - 1;
    }
}
