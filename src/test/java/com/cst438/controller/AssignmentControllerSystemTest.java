
// Authored by Jeremiah
// Covers System Test #1
// 1. Instructor adds a new assignment successfully

package com.cst438.controller;

import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssignmentControllerSystemTest {

  // edit the following to give the location and file name
  //  of the Chrome driver.
  //  for WinOS the file name will be chromedriver.exe
  //  for MacOS the file name will be chromedriver
  public static final String CHROME_DRIVER_FILE_LOCATION =
      "C:/chromedriver-win64/chromedriver.exe";

  //public static final String CHROME_DRIVER_FILE_LOCATION =
  //        "~/chromedriver_macOS/chromedriver";
  public static final String URL = "http://localhost:3000";

  public static final int SLEEP_DURATION = 1000; // 1 second.


  // add selenium dependency to pom.xml

  // there is also the assumption that the test data does NOT contain
  // an assignment with title "Test Assignment 1"
  // and dueDate "2024-03-01"
  // for courseId "cst363"

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

  // System Test #1 - Instructor adds a new assignment successfully
  @Test
  public void systemTestAddAssignment() throws Exception {
    // add an assignment for cst363 Spring 2024 term
    // verify assignment shows on the list of assignments for cst363 Spring 2024
    // delete the assignment
    // verify the assignment is gone

    TestUtils.assertInstructorHome(driver);

    // enter 2024, Spring and click show sections
    driver.findElement(By.id("year")).sendKeys("2024");
    driver.findElement(By.id("semester")).sendKeys("Spring");
    WebElement we = driver.findElement(By.id("sectionslink"));
    we.click();
    Thread.sleep(SLEEP_DURATION);

    // verify that cst363 is in the list of sections
    // if it exists, view assignments
    // Selenium throws NoSuchElementException when the element is not found
    try {
      while (true) {
        WebElement row363 = driver.findElement(By.xpath("//tr[td='M W 2:00-3:50']"));
        List<WebElement> links = row363.findElements(By.tagName("a"));
        // "Assignments" is the second link
        assertEquals(2, links.size());
        links.get(1).click(); // clicking on "Assignments" link
        Thread.sleep(SLEEP_DURATION);
      }
    } catch (NoSuchElementException e) {
      // do nothing, continue with test
    }

    // find and click button to add an assignment
    driver.findElement(By.id("addAssignment")).click();
    Thread.sleep(SLEEP_DURATION);

    // enter data
    //  title: Test Assignment 1,
    driver.findElement(By.id("etitle")).sendKeys("Test Assignment 1");
    //  dueDate: 2024-03-01,
    driver.findElement(By.id("eduedate")).sendKeys("2024-03-01");
    // click Save
    driver.findElement(By.id("save")).click();
    Thread.sleep(SLEEP_DURATION);

    String message = driver.findElement(By.id("message")).getText();
    assertTrue(message.startsWith("assignment added"));

    // verify that new Assignment shows up on Assignments list
    // find the row for Test Assignment 1
    WebElement rowTest = driver.findElement(By.xpath("//tr[td='Test Assignment 1']"));
    List<WebElement> buttons = rowTest.findElements(By.tagName("button"));
    // delete is the third button
    assertEquals(3, buttons.size());
    buttons.get(2).click();
    Thread.sleep(SLEEP_DURATION);
    // find the YES to confirm button
    List<WebElement> confirmButtons = driver
        .findElement(By.className("react-confirm-alert-button-group"))
        .findElements(By.tagName("button"));
    assertEquals(2, confirmButtons.size());
    confirmButtons.get(0).click();
    Thread.sleep(SLEEP_DURATION);

    // verify that Assignment list is now empty
    assertThrows(NoSuchElementException.class, () ->
        driver.findElement(By.xpath("//tr[td='Test Assignment 1']")));
  }
}
