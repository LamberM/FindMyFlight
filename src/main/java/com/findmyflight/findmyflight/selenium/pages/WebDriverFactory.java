package com.findmyflight.findmyflight.selenium.pages;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebDriverFactory {

    public WebDriver get() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");

        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        return driver;
    }
}
