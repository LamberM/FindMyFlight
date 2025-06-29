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
    public static final String MONTH_ATTRIBUTE_NAME = "qa-month";
    public static final String YEAR_ATTRIBUTE_NAME = "qa-year";
    public static final int MONTH_IN_YEAR = 12;
    private final String monthFieldSelector;
    private final String rightArrowFieldSelector;
    private final WebElement dateField;

    public BiletyLotniczeDatePicker(WebDriver webDriver, WebElement dateField, String monthFieldSelector, String rightArrowFieldSelector) {
        super(webDriver);
        this.monthFieldSelector = monthFieldSelector;
        this.rightArrowFieldSelector = rightArrowFieldSelector;
        this.dateField = dateField;
    }

    public void pickDate(LocalDate dateToPick) {
        dateField.click();
        waitUntilPresent(By.cssSelector(monthFieldSelector));
        WebElement currentDateElement = webDriver.findElement(By.cssSelector(monthFieldSelector));
        findDate(Integer.parseInt(currentDateElement.getAttribute(MONTH_ATTRIBUTE_NAME)), Integer.parseInt(currentDateElement.getAttribute(YEAR_ATTRIBUTE_NAME)), dateToPick);
    }

    private void findDate(int currentMonthIndex, int currentYearAttributeValue, LocalDate dateToPick) {
        var monthToPick = dateToPick.getMonthValue() - 1;
        var dayToPick = dateToPick.getDayOfMonth();
        var yearToPick = dateToPick.getYear();
        int monthDiff;
        if (isProperYear(currentYearAttributeValue, yearToPick)) {
            monthDiff = MONTH_IN_YEAR + monthToPick - currentMonthIndex;
            pickMonth(monthDiff, rightArrowFieldSelector);
        } else {
            monthDiff = monthToPick - currentMonthIndex;
            pickMonth(monthDiff, rightArrowFieldSelector);
        }
        List<WebElement> calendarDayElements = webDriver.findElements(By.cssSelector(AVAILABLE_DAYS_SELECTOR));
        for (WebElement element : calendarDayElements) {
            if (isCurrentDay(element, dayToPick)) {
                element.click();
                waitUntilDisappear(element);
            }
        }
    }

    private void pickMonth(int monthDiff, String rightArrowSelector) {
        WebElement rightArrowField;
        for (int i = 0; i < monthDiff; i++) {
            waitUntil(driver -> driver.findElement(By.cssSelector(rightArrowSelector)).isDisplayed(), Duration.ofSeconds(20));
            rightArrowField = webDriver.findElement(By.cssSelector(rightArrowSelector));
            rightArrowField.click();
        }
    }

    private boolean isProperYear(int currentYearAttributeValue, int yearToPick) {
        return yearToPick > currentYearAttributeValue;
    }

    private boolean isCurrentDay(WebElement element, int dayToPick) {
        return !element.getText().isEmpty() && Integer.parseInt(element.getText()) == dayToPick;
    }
}
