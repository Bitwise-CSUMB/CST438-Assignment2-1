// Authored by Chris
// Covers System Test #2
// Instructor grades an assignment and enters scores for all enrolled students and uploads the scores.

package com.cst438.controller;

import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.SectionRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StudentControllerSystemTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    private static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver-win64/chromedriver.exe";
    private static final String URL = "http://localhost:3000";
    private static final long SLEEP_DURATION = 1000; // milliseconds

    private ChromeDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
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

    // student enrolls into a section
    @Test
    public void systemTestAddCourse() throws Exception {

        TestUtils.assertStudentHome(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        // Click link to navigate to Enroll in Course page
        WebElement addCourse = driver.findElement(By.id("addCourse"));
        addCourse.click();
        WebElement courseTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));

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
