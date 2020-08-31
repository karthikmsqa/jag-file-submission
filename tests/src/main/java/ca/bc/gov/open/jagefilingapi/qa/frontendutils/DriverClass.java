package ca.bc.gov.open.jagefilingapi.qa.frontendutils;

import ca.bc.gov.open.jagefilingapi.qa.config.ReadConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DriverClass {

        public WebDriver driver;
        public Logger log = LogManager.getLogger(DriverClass.class);

        public void initializeDriver() throws IOException {
            ReadConfig readConfig = new ReadConfig();
            String  browser = readConfig.getBrowser();
            switch (browser) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    options.addArguments("--headless");
                    options.addArguments("--window-size=1920,1080");
                    driver = new ChromeDriver(options);
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    driver = new FirefoxDriver();
                    break;
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    driver = new EdgeDriver();
                    break;
                default:
                    log.info("URL value is not available in the properties file.");
            }
        }
        public void  driverSetUp() throws IOException {
            initializeDriver();

            driver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
            driver.manage().deleteAllCookies();
    }
}
