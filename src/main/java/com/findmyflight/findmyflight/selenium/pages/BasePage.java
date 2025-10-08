package com.findmyflight.findmyflight.selenium.pages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class BasePage {

    private static final Duration DEFAULT_WAIT_DURATION = Duration.ofSeconds(5);

    protected final WebDriver webDriver;

    protected void waitUntilPresent(By locator, Optional<Duration> duration) {
        var pickDuration = duration.orElse(DEFAULT_WAIT_DURATION);
        Wait<WebDriver> wait = new WebDriverWait(webDriver, pickDuration);
        wait.until(d -> !d.findElements(locator).isEmpty());
    }

    protected void waitUntilNotPresent(By locator, Duration duration) {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, duration);
        wait.until(d -> d.findElements(locator).isEmpty());
    }

    protected void waitUntilDisplayed(WebElement... elementsToWaitFor) {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, DEFAULT_WAIT_DURATION);
        for (WebElement e : elementsToWaitFor) {
            wait.until(d -> e.isDisplayed());
        }
    }

    protected void waitUntilDisappear(WebElement... elementsToWaitFor) {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, DEFAULT_WAIT_DURATION);
        for (WebElement e : elementsToWaitFor) {
            wait.until(d -> !e.isDisplayed());
        }
    }

    protected <T> void waitUntil(Function<WebDriver, T> condition, Duration duration) {
        Wait<WebDriver> wait = new WebDriverWait(webDriver, DEFAULT_WAIT_DURATION);
        wait.until(condition);
    }
}
