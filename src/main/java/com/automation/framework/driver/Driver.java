package com.automation.framework.driver;

import com.automation.framework.config.ConfigReader;
import com.automation.framework.exceptions.ViewNotLoadedException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared static facade for all browser access: creation, waits, element
 * interaction, JavaScript execution, tab switching, and shutdown.
 *
 * A ThreadLocal WebDriver keeps each execution thread isolated, which is
 * what makes parallel scenario execution (see the "parallel"/"parallelUnlimited"
 * Maven profiles) safe without one thread's actions leaking into another's
 * browser session.
 *
 * Local-vs-remote selection is driven by the "execution" system property
 * (local | remote): "remote" routes sessions through BrowserStack using
 * credentials injected via environment variables, never hardcoded here.
 */
public final class Driver {

    private static final Logger log = LogManager.getLogger(Driver.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private Driver() {
    }

    public static WebDriver get() {
        if (DRIVER.get() == null) {
            init();
        }
        return DRIVER.get();
    }

    public static void init() {
        boolean remote = "remote".equalsIgnoreCase(System.getProperty("execution", ConfigReader.get("execution", "local")));
        boolean headless = ConfigReader.getBoolean("headless", true);

        log.info("Initialising driver on thread [{}] (remote={}, headless={})",
                Thread.currentThread().getId(), remote, headless);

        WebDriver driver = remote ? createRemoteDriver() : createLocalDriver(headless);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("implicit.wait.seconds", 5)));
        driver.manage().window().maximize();

        DRIVER.set(driver);
    }

    private static WebDriver createLocalDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
        return new org.openqa.selenium.chrome.ChromeDriver(options);
    }

    /**
     * Builds a RemoteWebDriver pointed at BrowserStack. Credentials and
     * build/project naming come from environment variables and
     * application.properties respectively - never hardcoded here.
     */
    private static WebDriver createRemoteDriver() {
        String username = System.getenv("BROWSERSTACK_USERNAME");
        String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
        if (username == null || accessKey == null) {
            throw new IllegalStateException(
                    "BROWSERSTACK_USERNAME / BROWSERSTACK_ACCESS_KEY must be set for remote execution");
        }

        String hubUrl = "https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub";

        ChromeOptions options = new ChromeOptions();
        java.util.Map<String, Object> bstackOptions = new java.util.HashMap<>();
        bstackOptions.put("buildName", ConfigReader.get("browserstack.build.name", "local-build"));
        bstackOptions.put("projectName", ConfigReader.get("browserstack.project.name", "enterprise-ui-api-automation-framework"));
        bstackOptions.put("sessionName", System.getProperty("cucumber.scenario.name", "scenario"));
        options.setCapability("bstack:options", bstackOptions);

        try {
            return new RemoteWebDriver(new URL(hubUrl), options);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid BrowserStack hub URL", e);
        }
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
            log.info("Driver quit on thread [{}]", Thread.currentThread().getId());
        }
    }

    // ---- Waits & element interaction -------------------------------------------------

    private static WebDriverWait explicitWait() {
        return new WebDriverWait(get(), Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds", 10)));
    }

    public static WebElement waitVisible(By locator) {
        try {
            return explicitWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new ViewNotLoadedException("Element not visible within timeout: " + locator, e);
        }
    }

    public static WebElement waitClickable(By locator) {
        try {
            return explicitWait().until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            throw new ViewNotLoadedException("Element not clickable within timeout: " + locator, e);
        }
    }

    public static void click(By locator) {
        waitClickable(locator).click();
    }

    public static void type(By locator, String text) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    public static String text(By locator) {
        return waitVisible(locator).getText();
    }

    public static boolean isDisplayed(By locator) {
        try {
            return waitVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public static void dismissIfPresentInFrame(By frameLocator, By targetLocator, int timeoutSeconds) {
        WebDriver driver = get();
        try {
            WebElement frame = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(frameLocator));
            driver.switchTo().frame(frame);
            try {
                WebElement close = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                        .until(ExpectedConditions.elementToBeClickable(targetLocator));
                close.click();
            } finally {
                driver.switchTo().defaultContent();
            }
        } catch (TimeoutException e) {
            driver.switchTo().defaultContent();
        }
    }

    public static void dismissIfPresent(By locator, int timeoutSeconds) {
        try {
            WebElement element = new WebDriverWait(get(), Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        } catch (TimeoutException e) {
            // Not present - expected in the common case.
        }
    }

    public static void select(By locator, String visibleText) {
        new org.openqa.selenium.support.ui.Select(waitVisible(locator)).selectByVisibleText(visibleText);
    }

    public static void selectByValue(By locator, String value) {
        new org.openqa.selenium.support.ui.Select(waitVisible(locator)).selectByValue(value);
    }

    // ---- Navigation, JS execution, tab switching ---------------------------------------

    public static void navigateTo(String url) {
        get().get(url);
        log.info("Navigated to: {}", url);
    }

    public static Object executeJs(String script, Object... args) {
        return ((JavascriptExecutor) get()).executeScript(script, args);
    }

    public static void switchToNewWindow() {
        WebDriver driver = get();
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                return;
            }
        }
        log.warn("No new window/tab found to switch to");
    }

    // ---- Screenshots & BrowserStack status --------------------------------------------

    public static byte[] screenshotBytes() {
        return ((TakesScreenshot) get()).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Reports pass/fail status back to BrowserStack for the current session via
     * its JS executor API. No-op for local runs.
     */
    public static void markRemoteStatus(boolean passed, String reason) {
        if (!"remote".equalsIgnoreCase(System.getProperty("execution", "local"))) {
            return;
        }
        String status = passed ? "passed" : "failed";
        String script = String.format(
                "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": "
                        + "{\"status\":\"%s\", \"reason\": \"%s\"}}",
                status, reason == null ? "" : reason.replace("\"", "'"));
        try {
            executeJs(script);
        } catch (Exception e) {
            log.warn("Failed to report BrowserStack session status", e);
        }
    }

    public static List<String> openWindowHandles() {
        return new ArrayList<>(get().getWindowHandles());
    }
}
