
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
import com.cst438.dto.EnrollmentDTO;
import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT, properties="spring.h2.console.enabled=true")
public class EnrollmentControllerUnitTests {

    @Autowired
    private MockMvc mvc;

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

    @BeforeEach
    public void addDummyObjectsToDB() {

        testStart = TestUtils.getNow();

        // Make sure the dummy course does not already exist
        assertTrue(courseRepository.findById("cst999").isEmpty());

        // Add test Course to the db
        testCourse = courseRepository.save(new Course(
            "cst999",
            "General AI Systems",
            5
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
    }

    @AfterEach
    public void removeDummyObjectsFromDB() {
        enrollmentRepository.delete(testEnrollment2);
        userRepository.delete(testUser2);
        enrollmentRepository.delete(testEnrollment1);
        userRepository.delete(testUser1);
        sectionRepository.delete(testSection);
        termRepository.delete(testTerm);
        courseRepository.delete(testCourse);
    }

    private static <T> T updateEntity(Function<Integer, Optional<T>> findFunc, Supplier<Integer> idSupplier) {
        Optional<T> updated = findFunc.apply(idSupplier.get());
        assertTrue(updated.isPresent());
        return updated.get();
    }

    private static EnrollmentDTO changeGrade(EnrollmentDTO enrollment, String newGrade) {
        return new EnrollmentDTO(
            enrollment.enrollmentId(),
            newGrade,
            enrollment.studentId(),
            enrollment.name(),
            enrollment.email(),
            enrollment.courseId(),
            enrollment.courseTitle(),
            enrollment.sectionId(),
            enrollment.sectionNo(),
            enrollment.building(),
            enrollment.room(),
            enrollment.times(),
            enrollment.credits(),
            enrollment.year(),
            enrollment.semester()
        );
    }

    // Unit Test 10 - EnrollmentController::getEnrollments() and EnrollmentController::updateEnrollmentGrade()
    @Test
    public void test() throws Exception {

        // Get enrollments using REST API
        MockHttpServletResponse response = mvc.perform(
            MockMvcRequestBuilders
                .get(String.format("/sections/%s/enrollments", testSection.getSectionNo()))
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Check that the status code + message are as expected
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());

        // Convert response to EnrollmentDTO list
        List<EnrollmentDTO> enrollments = TestUtils.fromJsonListString(
            response.getContentAsString(), EnrollmentDTO.class);

        // Update grades locally

        List<EnrollmentDTO> newEnrollments = new ArrayList<>();

        for (EnrollmentDTO enrollment : enrollments) {
            newEnrollments.add(changeGrade(enrollment, "A"));
        }

        // Update the enrollments using the REST API
        response = mvc.perform(
            MockMvcRequestBuilders
                .put("/enrollments")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(newEnrollments))
        ).andReturn().getResponse();

        // Check that the status code + message are as expected
        assertEquals(200, response.getStatus());
        assertNull(response.getErrorMessage());

        // Re-fetch testEnrollment1 and testEnrollment2 from the database
        testEnrollment1 = updateEntity(enrollmentRepository::findById, testEnrollment1::getEnrollmentId);
        testEnrollment2 = updateEntity(enrollmentRepository::findById, testEnrollment2::getEnrollmentId);

        // Check that the grades were really updated
        assertEquals(testEnrollment1.getGrade(), "A");
        assertEquals(testEnrollment2.getGrade(), "A");
    }
}
