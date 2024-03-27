
// Authored by Jake
// Covers System Test #4
// 4. Instructor enters a final grade for an enrollment in a section

package com.cst438.controller;

import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnrollmentControllerSystemTest {

    // TODO edit the following to give the location and file name
    //  of the Chrome driver.
    //  for WinOS the file name will be chromedriver.exe
    //  for MacOS the file name will be chromedriver
    private static final String CHROME_DRIVER_FILE_LOCATION =
        "C:/chromedriver-win64/chromedriver.exe";

    private static final String URL = "http://localhost:3000";

    private static final int SLEEP_DURATION = 1000; // 1 second

    private WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // Set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // Start the driver
        driver = new ChromeDriver(ops);
        driver.get(URL);

        // Must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // Quit the driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    private void navigateToEnrollments() throws Exception {

        // Enter "2024", "Spring", and click "Show Sections"
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("sectionslink")).click();
        Thread.sleep(SLEEP_DURATION);

        // Find "cst438" row
        final WebElement testCourseRow = driver.findElement(By.xpath("//tr/td[text()='cst438']/.."));

        // Find "cst438" row's "Enrollments" link
        final WebElement enrollmentLink = testCourseRow.findElement(By.xpath("td/a[text()='Enrollments']"));

        // Click "Enrollments"
        enrollmentLink.click();
        Thread.sleep(SLEEP_DURATION);
    }

    private WebElement getThomasGradeElement() {
        final WebElement row = driver.findElement(By.xpath("//tr/td[text()='thomas edison']/.."));
        return row.findElement(By.xpath("td//input[@name='grade']"));
    }

    private void saveAndVerifyEnrollments() throws Exception {

        // Click "Save Changes"
        driver.findElement(By.id("saveChanges")).click();
        Thread.sleep(SLEEP_DURATION);

        // Check that the database returned the expected message
        final WebElement errorText = driver.findElement(By.className("Error"));
        assertEquals("Enrollments saved", errorText.getText());
    }

    // System Test #4 - Instructor enters a final grade for an enrollment in a section
    @Test
    public void systemTestGradeEnrollment() throws Exception {

        TestUtils.assertInstructorHome(driver);

        // Navigate to enrollments page
        navigateToEnrollments();

        // Set Thomas Edison's grade
        WebElement thomasGradeElement = getThomasGradeElement();
        final String oldGradeValue = thomasGradeElement.getAttribute("value");
        final String newGradeValue = oldGradeValue.equals("A") ? "B" : "A";
        thomasGradeElement.clear();
        thomasGradeElement.sendKeys(newGradeValue);

        // Click "Save Changes" and check that the database returned the expected message
        saveAndVerifyEnrollments();

        // Go back to home page
        driver.findElement(By.id("home")).click();

        // Navigate to enrollments page
        navigateToEnrollments();

        // Check that the grade was persisted
        thomasGradeElement = getThomasGradeElement();
        assertEquals(newGradeValue, thomasGradeElement.getAttribute("value"));

        // Restore old grade value
        thomasGradeElement.clear();
        thomasGradeElement.sendKeys(oldGradeValue);

        // Click "Save Changes" and check that the database returned the expected message
        saveAndVerifyEnrollments();
    }
}
