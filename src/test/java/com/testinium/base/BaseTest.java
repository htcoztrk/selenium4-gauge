package com.testinium.base;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.testinium.driver.TestiniumSeleniumDriver;
import com.testinium.model.ElementInfo;
import com.thoughtworks.gauge.AfterSuite;
import com.thoughtworks.gauge.BeforeSuite;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class BaseTest {

    protected static WebDriver driver;
    protected static Actions actions;

    private static final String DEFAULT_DIRECTORY_PATH = "elementValues";
    private static final ConcurrentMap<String, Object> elementMap = new ConcurrentHashMap<>();
    private static volatile boolean elementsInitialized = false;

    @BeforeSuite
    public void setUp() {
        log.info("========== Gauge BeforeSuite: Initializing Testinium WebDriver ==========");

        try {
            String remoteUrl = getEnvOrDefault("SELENIUM_REMOTE_URL", "http://172.25.1.110:4444/wd/hub");
            String testiniumKey = getEnvOrDefault("TESTINIUM_KEY", "varsayilan_deger");

            ChromeOptions options = buildChromeOptions(testiniumKey);

            driver = new TestiniumSeleniumDriver(new URL(remoteUrl), options);
            actions = new Actions(driver);

            // Load element repository once (safe)
            initElementsOnce();

            log.info("WebDriver initialized successfully. Remote URL: {}", remoteUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Testinium WebDriver", e);
        }
    }

    @AfterSuite
    public void tearDown() {
        log.info("========== Gauge AfterSuite: Quitting WebDriver ==========");
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private ChromeOptions buildChromeOptions(String testiniumKey) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("disable-translate");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--allow-cross-origin-auth-prompt");

        Map<String, Object> prefs = new HashMap<>();
        options.setExperimentalOption("prefs", prefs);

        options.setCapability("testinium:key", testiniumKey);
        return options;
    }

    private String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            log.warn("Environment variable '{}' is not set. Using default value: {}", name, defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    // ---------- Element Repository (JSON) ----------

    private static synchronized void initElementsOnce() {
        if (elementsInitialized) return;

        File[] fileList = getFileListStatic();
        if (fileList.length == 0) {
            log.warn("No element JSON files found under classpath folder '{}'. Skipping element repository init.", DEFAULT_DIRECTORY_PATH);
            elementsInitialized = true; // prevent repeated scanning
            return;
        }

        initElementMapStatic(fileList);
        elementsInitialized = true;

        log.info("Element repository initialized. Total keys: {}", elementMap.size());
    }

    private static void initElementMapStatic(File[] fileList) {
        Type elementType = new TypeToken<List<ElementInfo>>() {}.getType();
        Gson gson = new Gson();

        for (File file : fileList) {
            try {
                List<ElementInfo> elementInfoList = gson.fromJson(new FileReader(file), elementType);
                if (elementInfoList == null) continue;

                for (ElementInfo elementInfo : elementInfoList) {
                    elementMap.put(elementInfo.getKey(), elementInfo);
                }
            } catch (FileNotFoundException e) {
                log.warn("Element file not found: {}", file.getName(), e);
            } catch (Exception e) {
                log.warn("Failed to parse element file: {}", file.getName(), e);
            }
        }
    }

    private static File[] getFileListStatic() {
        URL url = BaseTest.class.getClassLoader().getResource(DEFAULT_DIRECTORY_PATH);
        if (url == null) {
            log.warn("Element directory not found on classpath: {}", DEFAULT_DIRECTORY_PATH);
            return new File[0];
        }

        File[] fileList = new File(url.getFile())
                .listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".json"));

        return fileList != null ? fileList : new File[0];
    }


    public static WebDriver getDriver() {
        return driver;
    }

    public static Actions getActions() {
        return actions;
    }

    public static ElementInfo findElementInfoByKey(String key) {
        Object v = elementMap.get(key);
        return (v instanceof ElementInfo) ? (ElementInfo) v : null;
    }

    public static void saveValue(String key, String value) {
        elementMap.put(key, value);
    }

    public static String getValue(String key) {
        Object v = elementMap.get(key);
        return v != null ? v.toString() : null;
    }
}
