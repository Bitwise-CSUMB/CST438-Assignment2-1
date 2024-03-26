
package com.cst438.controller;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
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

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private UserRepository userRepository;

    private LocalDateTime testStart;

    private Course testCourse;

    private Term testTerm;

    private Section testSection;

    private User testUser1;

    private Enrollment testEnrollment1;

    private User testUser2;

    private Enrollment testEnrollment2;

    private void setUpDriver() throws Exception {

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

    private void terminateDriver() {
        if (driver != null) {
            // Quit the driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @BeforeEach
    public void beforeEach() throws Exception {

        testStart = TestUtils.getNow();

        // Make sure the dummy course does not already exist
        assertTrue(courseRepository.findById("cst999").isEmpty());

        // Add test Course to the db
        testCourse = courseRepository.save(new Course(
            "cst999",             // String courseId
            "General AI Systems", // String title
            5                     // int credits
        ));

        // Add test Term to the db
        testTerm = termRepository.save(new Term(
            -1,                                                         // int termId
            testStart.getYear(),                                        // int year
            "Spring",                                                   // String semester
            TestUtils.getSqlDate(testStart),                            // Date addDate
            TestUtils.getSqlDate(testStart.plusMonths(1)),              // Date addDeadline
            TestUtils.getSqlDate(testStart.plusMonths(1).plusWeeks(1)), // Date dropDeadline
            TestUtils.getSqlDate(testStart.plusMonths(1)),              // Date startDate
            TestUtils.getSqlDate(testStart.plusMonths(1).plusWeeks(18)) // Date endDate
        ));

        // Add test Section to the db
        testSection = sectionRepository.save(new Section(
            -1,                    // int sectionNo
            testCourse,            // Course course
            testTerm,              // Term term
            1,                     // int secId
            "052",                 // String building
            "222",                 // String room
            "T Th 12:00-1:50",     // String times
            "dwisneski@csumb.edu", // String instructorEmail
            new ArrayList<>(),     // List<Enrollment> enrollments
            new ArrayList<>()      // List<Assignment> assignments
        ));

        // Add test User 1 to the db
        testUser1 = userRepository.save(new User(
            -1,                   // int id
            "John Doe",           // String name
            "john_doe@csumb.edu", // String email
            "hunter2",            // String password
            "STUDENT"             // String type
        ));

        // Add test Enrollment 1 to the db
        testEnrollment1 = enrollmentRepository.save(new Enrollment(
            -1,               // int enrollmentId
            null,             // String grade
            testUser1,        // User user
            testSection,      // Section section
            new ArrayList<>() // List<Grade> grades
        ));

        // Add test User 2 to the db
        testUser2 = userRepository.save(new User(
            -1,                   // int id
            "Jane Doe",           // String name
            "jane_doe@csumb.edu", // String email
            "hunter2",            // String password
            "STUDENT"             // String type
        ));

        // Add test Enrollment 2 to the db
        testEnrollment2 = enrollmentRepository.save(new Enrollment(
            -1,               // int enrollmentId
            null,             // String grade
            testUser2,        // User user
            testSection,      // Section section
            new ArrayList<>() // List<Grade> grades
        ));

        // launch driver
        setUpDriver();
    }

    @AfterEach
    public void afterEach() {

        terminateDriver();

        enrollmentRepository.delete(testEnrollment2);
        userRepository.delete(testUser2);
        enrollmentRepository.delete(testEnrollment1);
        userRepository.delete(testUser1);
        sectionRepository.delete(testSection);
        termRepository.delete(testTerm);
        courseRepository.delete(testCourse);
    }

    @Test
    public void systemTestGradeEnrollment() throws Exception {

        // Enter test year, "Spring", and click "Show Sections"
        driver.findElement(By.id("year")).sendKeys(String.valueOf(testStart.getYear()));
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("sectionslink")).click();
        Thread.sleep(SLEEP_DURATION);

        // Find cst999 row
        final WebElement testCourseRow = driver.findElement(By.xpath(String.format(
            "//tr/td[text()='%s']/..", testCourse.getCourseId())));

        // Find cst999 row's "Enrollments" link
        final WebElement enrollmentLink = testCourseRow.findElement(By.xpath("td/a[text()='Enrollments']"));

        // Click "Enrollments"
        enrollmentLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Set grades
        final List<WebElement> gradeFields = driver.findElements(By.name("grade"));
        for (WebElement gradeField : gradeFields) {
            gradeField.clear();
            gradeField.sendKeys("A");
        }

        // Click "Save Changes"
        driver.findElement(By.id("saveChanges")).click();
        Thread.sleep(SLEEP_DURATION);

        // Re-fetch testEnrollment1 and testEnrollment2 from the database
        testEnrollment1 = TestUtils.updateEntity(enrollmentRepository::findById, testEnrollment1::getEnrollmentId);
        testEnrollment2 = TestUtils.updateEntity(enrollmentRepository::findById, testEnrollment2::getEnrollmentId);

        // Check that the grades were really updated
        assertEquals("A", testEnrollment1.getGrade());
        assertEquals("A", testEnrollment2.getGrade());
    }
}
