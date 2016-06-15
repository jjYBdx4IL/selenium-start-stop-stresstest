/*
 * Copyright (C) 2016 jjYBdx4IL (https://github.com/jjYBdx4IL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jjYBdx4IL.test.selenium;

import com.github.jjYBdx4IL.test.AdHocHttpServer;
import com.github.jjYBdx4IL.utils.ProcRunner;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *
 * @author jjYBdx4IL
 */
public class SeleniumTestBaseTest {

    private static WebDriver driver;
    protected static final int CLICK_WAIT4ELEMENT_MILLIS = 120 * 1000;
    protected static final int CLICK_WAIT4ELEMENT_POLL_MILLIS = 2 * 1000;

    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger(SeleniumTestBaseTest.class.getName());
    protected static AdHocHttpServer server = null;
    protected static URL testPage1 = null;
    protected final static String testPage1Content
            = "<html><head></head><body>"
            + "  <select class=\"gwt-ListBox\" name=\"selectName\">"
            + "    <option value=\"Select a topic:\">Select a topic:</option>"
            + "    <option value=\"GenericVal\">Generic</option>"
            + "    <option value=\"OSD App\">OSD App</option>"
            + "  </select>"
            + "  <input name=\"inputName1\" type=\"text\">"
            + "</body></html>";

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new AdHocHttpServer();
        testPage1 = server.addStaticContent("/testPage1",
                new AdHocHttpServer.StaticResponse(testPage1Content));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (server != null) {
            server.close();
            server = null;
        }
        stopDriver();
    }

    @Test
    public void testStress() throws WebElementNotFoundException, InterruptedException, IOException {
        log.info("DBUS_SESSION_BUS_ADDRESS=" + System.getenv("DBUS_SESSION_BUS_ADDRESS"));

        for (int i = 0; i < 100; i++) {
            getDriver().get(testPage1.toExternalForm());
            waitForElement("xpath://input[@name='inputName1']");
            stopDriver();
        }

        // verify proper browser shutdown:
        ProcRunner runner = new ProcRunner("ps", "xafu");
        assertEquals(0, runner.run());
        String psOutput = runner.getOutputBlob();
        assertEquals(psOutput, -1, psOutput.indexOf("chromium"));
        assertEquals(psOutput, -1, psOutput.indexOf("chrome"));
        assertEquals(psOutput, -1, psOutput.indexOf("google-chrome"));
    }

    public static void stopDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static WebDriver getDriver() {
        if (driver == null) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setBinary("/usr/bin/chromium-browser");
            driver = new ChromeDriver(chromeOptions);
        }
        return driver;
    }

    /**
     * Wait until there is some element returned by {@link #findElement} which is displayed and enabled. The
     * timeout is given by {@link #CLICK_WAIT4ELEMENT_MILLIS}.
     *
     * @param text the text value of the element to select
     * @return
     * @throws WebElementNotFoundException
     */
    public WebElement waitForElement(String text, Boolean displayed, Boolean enabled)
            throws WebElementNotFoundException {
        log.info("waitForElement(" + text + ")");

        WebElement e = null;
        long timeout = System.currentTimeMillis() + CLICK_WAIT4ELEMENT_MILLIS;
        do {
            try {
                e = findElement(text);
                if (e != null && enabled != null && enabled.booleanValue() != e.isEnabled()) {
                    e = null;
                }
                if (e != null && displayed != null && displayed.booleanValue() != e.isDisplayed()) {
                    e = null;
                }
            } catch (StaleElementReferenceException ex) {
                e = null;
            }
            if (e == null) {
                try {
                    Thread.sleep(CLICK_WAIT4ELEMENT_POLL_MILLIS);
                } catch (InterruptedException ex) {
                }
            }
        } while ((e == null) && System.currentTimeMillis() < timeout);
        if (e == null) {
            throw new WebElementNotFoundException(text);
        }
        return e;
    }

    public WebElement waitForElement(String text) throws WebElementNotFoundException {
        return waitForElementDisplayedAndEnabled(text);
    }

    public WebElement waitForElementDisplayedAndEnabled(String text) throws WebElementNotFoundException {
        return waitForElement(text, true, true);
    }

    public WebElement waitForElementDisplayedAndDisabled(String text) throws WebElementNotFoundException {
        return waitForElement(text, true, false);
    }

    public WebElement findElement(String text) {
        List<WebElement> elements;
        if (text.startsWith("xpath:")) {
            elements = getDriver().findElements(By.xpath(text.substring("xpath:".length())));
            if (elements.size() > 0) {
                return elements.get(0);
            }
            return null;
        }
        elements = getDriver().findElements(By.xpath("//button[text()='" + text + "']"));
        if (elements.size() > 0) {
            return elements.get(0);
        }
        elements = getDriver().findElements(By.xpath("//label[text()='" + text + "']"));
        if (elements.size() > 0) {
            return elements.get(0);
        }
        elements = getDriver().findElements(By.xpath("//div[text()='" + text + "']"));
        if (elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

}
