
// Authored by Andi
// Covers System Test #3
// 3. Student enrolls into a section

package com.cst438.controller;

import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StudentControllerSystemTest {

    private static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver-win64/chromedriver.exe";
    private static final String URL = "http://localhost:3000";
    private static final long SLEEP_DURATION = 1000; // milliseconds

    private ChromeDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);
        driver.get(URL);

        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    // System Test #3 - Student enrolls into a section
    @Test
    public void systemTestAddCourse() {

        TestUtils.assertStudentHome(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        // Click link to navigate to Enroll in Course page
        WebElement addCourse = driver.findElement(By.id("addCourse"));
        addCourse.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));

        // Wait until the Enroll button is clickable
        WebElement enrollButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("Enroll")));
        enrollButton.click();

        // Get the selectedCourseSecNo
        WebElement selectedRow = enrollButton.findElement(By.xpath("./ancestor::tr"));
        String sectionNumber = selectedRow.findElement(By.xpath("./td[2]")).getText();

        // Wait until the Confirm button is clickable
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("Confirm")));
        confirmButton.click();

        // Click link to navigate to View Schedule page
        WebElement scheduleLink = driver.findElement(By.id("schedule"));
        scheduleLink.click();
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        WebElement queryButton = driver.findElement(By.id("query"));
        queryButton.click();
        WebElement scheduleTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));

        // Verify that new section shows up on schedule table
        List<WebElement> scheduleRows = scheduleTable.findElements(By.tagName("tr"));
        boolean sectionFound = false;
        for (WebElement row : scheduleRows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (WebElement cell : cells) {
                if (cell.getText().equals(sectionNumber)) {
                    sectionFound = true;
                    break;
                }
            }
            if (sectionFound) {
                break;
            }
        }

        // Assertion for section presence in schedule
        assertTrue(sectionFound, "Enrolled section found");
    }
}
