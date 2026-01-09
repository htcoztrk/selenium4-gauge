package com.testinium.step;

import com.testinium.base.BaseTest;
import com.testinium.model.ElementInfo;
import com.thoughtworks.gauge.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class BaseSteps extends BaseTest {


    public BaseSteps() {
        // Driver BeforeSuite'de init edilir; burada sadece referans alıyoruz.
        this.driver = BaseTest.getDriver();
        this.actions = BaseTest.getActions();
    }

    WebElement findElement(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(60));        WebElement webElement = webDriverWait
                .until(ExpectedConditions.presenceOfElementLocated(infoParam));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})",
                webElement);
        return webElement;
    }

    public By getElementInfoToBy(ElementInfo elementInfo) {
        By by = null;
        if (elementInfo.getType().equals("css")) {
            by = By.cssSelector(elementInfo.getValue());
        } else if (elementInfo.getType().equals(("name"))) {
            by = By.name(elementInfo.getValue());
        } else if (elementInfo.getType().equals("id")) {
            by = By.id(elementInfo.getValue());
        } else if (elementInfo.getType().equals("xpath")) {
            by = By.xpath(elementInfo.getValue());
        } else if (elementInfo.getType().equals("linkText")) {
            by = By.linkText(elementInfo.getValue());
        } else if (elementInfo.getType().equals(("partialLinkText"))) {
            by = By.partialLinkText(elementInfo.getValue());
        }
        return by;
    }

    private void clickElement(WebElement element) {
        element.click();
    }


    private void hoverElement(WebElement element) {
        actions.moveToElement(element).build().perform();
    }







    @Step({"Go to <url> address",
            "<url> adresine git"})
    public void goToUrl(String url) {
        driver.get(url);
        log.info(url + " adresine gidiliyor.");
    }

    @Step("Şu anki url <url> ile aynı mı")
    public void checkCurrentUrlEquals(String url) {
        String currentUrl = driver.getCurrentUrl();
        assertEquals(currentUrl, url);
        log.info("Şu anki url " + url + " ile aynı.");
    }

    @Step({"Wait <seconds> seconds", "<seconds> saniye bekle"})
    public void waitBySeconds(int seconds) {
        try {
            log.info("{} seconds waiting...", seconds);
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }

    @Step("<key> elementinin görünür olması kontrol edilir")
    public void checkElementIsVisible(String key) {
        WebElement element = findElement(key);
        assertTrue(element.isDisplayed());
        log.info(key + " elementi görünür durumda.");
    }

    @Step({"Click to element <key>",
            "<key> elementine tıkla"})
    public void clickElement(String key) {
        if (!key.isEmpty()) {
            hoverElement(findElement(key));
            clickElement(findElement(key));
            log.info(key + " elementine tıklandı.");
        }
    }






}










