
package com.cst438.controller;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.dto.AssignmentDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TermRepository termRepository;

    private LocalDateTime testStart;

    private Course testCourse;

    private Term testTerm;

    private Section testSection;

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
    }

    @AfterEach
    public void removeDummyObjectsFromDB() {
        sectionRepository.delete(testSection);
        termRepository.delete(testTerm);
        courseRepository.delete(testCourse);
    }

    // Unit Test 3 - AssignmentController::createAssignment() - Adding new assignment with invalid section number
    @Test
    public void addAssignmentInvalidSectionNumber() throws Exception {

        final int invalidSecNo = -1;

        // Assignment used to test invalid sectionNo reference
        final AssignmentDTO assignment = new AssignmentDTO(
            -1,                                                            // int id
            "Week 1 - Learning Journal",                                   // String title
            testStart.plusMonths(1).plusWeeks(1).toLocalDate().toString(), // String dueDate
            testCourse.getCourseId(),                                      // String courseId
            testSection.getSecId(),                                        // int secId
            invalidSecNo                                                   // int secNo - Intentionally invalid
        );

        // Try to add the assignment using the REST API
        final MockHttpServletResponse response = mvc.perform(
            MockMvcRequestBuilders
                .post("/assignments")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(assignment))
        ).andReturn().getResponse();

        // Check that the status code + message are as expected
        assertEquals(404, response.getStatus());
        assertEquals("Section " + invalidSecNo + " not found", response.getErrorMessage());
    }
}
