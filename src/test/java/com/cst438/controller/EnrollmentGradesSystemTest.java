
// Authored by Chris
// Covers System Test #2
// 2. Instructor grades an assignment, enters scores for all enrolled students, and uploads the scores

package com.cst438.controller;

import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EnrollmentGradesSystemTest {

    // TODO edit the following to give the location and file name
    // of the Chrome driver.
    //  for WinOS the file name will be chromedriver.exe
    //  for MacOS the file name will be chromedriver
    public static final String CHROME_DRIVER_FILE_LOCATION =
            "C:/chromedriver-win64/chromedriver.exe";

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

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

    // System Test #2 - Instructor grades an assignment, enters scores for all enrolled students, and uploads the scores
    @Test
    public void systemTestGradeAssignment() throws Exception {

        // Verify that App.js is set to INSTRUCTOR.
        TestUtils.assertInstructorHome(driver);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("sectionslink")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.xpath("(//a)[2]")).click();
        Thread.sleep(SLEEP_DURATION);

        // Grade the target grades current value, alter it, and confirm.
        WebElement gradeWe = driver.findElement(By.xpath("//tr/td[5]/div/div/input"));
        String oldGrade = gradeWe.getAttribute("value");

        gradeWe.sendKeys(Keys.BACK_SPACE);
        String newGrade;
        if (gradeWe.getAttribute("value") == "F")
            newGrade = "A";
        else
            newGrade = "F";
        gradeWe.sendKeys(newGrade);
        Thread.sleep(SLEEP_DURATION);

        assertNotEquals(oldGrade, gradeWe.getAttribute("value"));
        assertEquals(newGrade, gradeWe.getAttribute("value"));

        driver.findElement(By.id("saveChanges")).click();
        Thread.sleep(SLEEP_DURATION);

        // Verifying the grade was successfully saved
        WebElement errorText = driver.findElement(By.className("Error"));
        assertEquals("Enrollments saved", errorText.getText());

        driver.findElement(By.id("home")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("sectionslink")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.xpath("(//a)[2]")).click();
        Thread.sleep(SLEEP_DURATION);

        gradeWe = driver.findElement(By.xpath("//tr/td[5]/div/div/input"));
        Thread.sleep(SLEEP_DURATION);

        assertNotEquals(oldGrade, gradeWe.getAttribute("value"));
        assertEquals(newGrade, gradeWe.getAttribute("value"));

        gradeWe.sendKeys(Keys.BACK_SPACE);
        gradeWe.sendKeys(oldGrade);

        driver.findElement(By.id("saveChanges")).click();

        driver.findElement(By.id("home")).click();
    }
}