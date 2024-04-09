
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

import java.util.ArrayList;

public class EnrollmentGradesSystemTest {

    // edit the following to give the location and file name
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

    // System Test #2 - Instructor grades an assignment, enters scores for all enrolled students, and uploads the scores
    @Test
    public void systemTestGradeAssignment() throws Exception {

        // Verify that App.js is set to INSTRUCTOR.
        TestUtils.assertInstructorHome(driver);

        // Search for the sections in Spring 2024.
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("sectionslink")).click();
        Thread.sleep(SLEEP_DURATION);

        // We're selecting the assignments from the first section.
        driver.findElement(By.xpath("(//a)[3]")).click();
        Thread.sleep(SLEEP_DURATION);

        // Grading the first assignment.
        WebElement assignmentTable = driver.findElement(By.id("assignmentTable"));
        WebElement viewAssignmentsBtn = assignmentTable.findElement(By.xpath("//tr/td[4]"));
        viewAssignmentsBtn.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement gradeTable = driver.findElement(By.id("gradeTable"));
        ArrayList<WebElement> gradeList = new ArrayList<>(gradeTable.findElements(By.name("score")));

        // Replacing all current scores with new temporary scores.
        String[] oldScores = new String[gradeList.size()];
        String[] newScores = new String[gradeList.size()];
        for (int i = 0; i < gradeList.size(); i++) {
            WebElement grade = gradeList.get(i);
            oldScores[i] = grade.getAttribute("value");
            newScores[i] = oldScores[i].equals("55") ? "45" : "55";

            assertNotEquals(newScores[i], oldScores[i]);
            assertEquals(oldScores[i], grade.getAttribute("value"));

            grade.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            grade.sendKeys(newScores[i]);

            assertEquals(newScores[i], grade.getAttribute("value"));

            Thread.sleep(SLEEP_DURATION);
        }
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("gradeDialogSave")).click();
        Thread.sleep(SLEEP_DURATION);

        viewAssignmentsBtn.click();
        Thread.sleep(SLEEP_DURATION);

        gradeTable = driver.findElement(By.id("gradeTable"));
        gradeList = new ArrayList<>(gradeTable.findElements(By.name("score")));

        // Verifying the change was saved successfully and reverting it.
        for (int i = 0; i < gradeList.size(); i++) {
            WebElement grade = gradeList.get(i);

            assertEquals(newScores[i], grade.getAttribute("value"));

            grade.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            grade.sendKeys(oldScores[i]);

            assertEquals(oldScores[i], grade.getAttribute("value"));

            Thread.sleep(SLEEP_DURATION);
        }

        driver.findElement(By.id("gradeDialogSave")).click();
        Thread.sleep(SLEEP_DURATION);
    }
}
