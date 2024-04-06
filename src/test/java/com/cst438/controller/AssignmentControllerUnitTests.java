
// Authored by Jake, Jeremiah, Chris
// Covers Unit Tests #1, #2, #3, #4, #5
// 1. Instructor adds a new assignment successfully
// 2. Instructor adds a new assignment with a due date past the end date of the class
// 3. Instructor adds a new assignment with an invalid section number
// 4. Instructor grades an assignment, enters scores for all enrolled students, and uploads the scores
// 5. Instructor attempts to grade an assignment but the assignment id is invalid

package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
import com.cst438.test.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.cst438.test.utils.TestUtils.asJsonString;
import static com.cst438.test.utils.TestUtils.fromJsonString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TermRepository termRepository;

    private LocalDateTime testStart;

    private Course testCourse;

    private Term testTerm;

    private Section testSection;

    @BeforeEach
    public void addDummyObjectsToDB(TestInfo testInfo) {

        if (!testInfo.getTags().contains("Dummy-Data")) {
            return;
        }

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
    }

    @AfterEach
    public void removeDummyObjectsFromDB(TestInfo testInfo) {

        if (!testInfo.getTags().contains("Dummy-Data")) {
            return;
        }

        sectionRepository.delete(testSection);
        termRepository.delete(testTerm);
        courseRepository.delete(testCourse);
    }

    // Unit Test #1 - Instructor adds a new assignment successfully
    //   AssignmentController::createAssignment()
    @Test
    public void addAssignment() throws Exception {
        MockHttpServletResponse response;

        // create DTO with data for new assignment.
        // the primary key, id, is set to 0. it will be
        // set by the database when the assignment is inserted.
        AssignmentDTO assignment = new AssignmentDTO(
            0,
            "Test Assignment 1",
            "2024-03-01",
            "cst363",
            1,
            8
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert assignment to String data and set as request content
        response = mvc.perform(
                MockMvcRequestBuilders
                    .post("/assignments")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(assignment)))
            .andReturn()
            .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);

        // primary key should have a non-zero value from the database
        assertNotEquals(0, result.id());
        // check other fields of the DTO for expected values
        assertEquals("Test Assignment 1", result.title());
        assertEquals("2024-03-01", result.dueDate());
        assertEquals("cst363", result.courseId());
        assertEquals(1, result.secId());
        assertEquals(8, result.secNo());

        // check the database
        Assignment a = assignmentRepository.findByAssignmentId(result.id());
        assertNotNull(a);
        assertEquals("cst363", a.getSection().getCourse().getCourseId());

        // clean up after test. issue http DELETE request for assignment
        response = mvc.perform(
                MockMvcRequestBuilders
                    .delete("/assignments/"+result.id()))
            .andReturn()
            .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        a = assignmentRepository.findByAssignmentId(result.id());
        assertNull(a);  // assignment should not be found after delete
    }

    // Unit Test #2 - Instructor adds a new assignment with a due date past the end date of the class
    //   AssignmentController::createAssignment()
    @Test
    public void addAssignmentBadDueDate() throws Exception {
        MockHttpServletResponse response;

        // dueDate "2100-03-01" is past the end date of the class
        AssignmentDTO assignment = new AssignmentDTO(
            0,
            "Test Assignment 1",
            "2100-03-01",
            "cst363",
            1,
            8
        );

        // issue the POST request
        response = mvc.perform(
                MockMvcRequestBuilders
                    .post("/assignments")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(assignment)))
            .andReturn()
            .getResponse();

        // response should be 400, BAD_REQUEST
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Bad due date; section 8 timeframe: 2024-01-15 - 2024-05-17", message);
    }

    // Unit Test #3 - Instructor adds a new assignment with an invalid section number
    //   AssignmentController::createAssignment()
    @Test
    @Tag("Dummy-Data")
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

    // Unit Test #4 - Instructor grades an assignment, enters scores for all enrolled students, and uploads the scores
    @Test
    public void gradeAssignment() throws Exception {
        MockHttpServletResponse response;

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/1/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        assertEquals(200, response.getStatus());

        GradeDTO[] result = fromJsonString(response.getContentAsString(), GradeDTO[].class);
        int numDTOs = result.length;

        // List of grades grabbed from database shouldn't be empty.
        assertNotEquals(0, numDTOs);

        /*
         * Pulling old scores, altering them, and saving a new list of GradeDTOs.
         */
        Integer[] oldScores = new Integer[numDTOs];
        Integer[] newScores = new Integer[numDTOs];
        GradeDTO[] newDTOs = new GradeDTO[numDTOs];
        for (int i = 0; i < numDTOs; i++) {
            GradeDTO target = result[0];
            oldScores[i] = target.score();

            newScores[i] = (oldScores[i] == 55) ? 25 : 55;
            newDTOs[i] = new GradeDTO(target.gradeId(), target.studentName(), target.studentEmail(), target.assignmentTitle(), target.courseId(), target.sectionId(), newScores[i]);
        }

        // Confirming that all scores were altered successfully.
        for (int i = 0; i < numDTOs; i++) {
            assertNotEquals(newScores[i], oldScores[i]);
            assertNotEquals(oldScores[i], newDTOs[i].score());
            assertEquals(newScores[i], newDTOs[i].score());
        }

        // Saving all altered GradeDTOs.
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(newDTOs)))
                        .andReturn()
                        .getResponse();
        
        assertEquals(200, response.getStatus());

        // Confirming that all DTOs were saved successfully.
        for (int i = 0; i < numDTOs; i++) {
            Grade g = gradeRepository.findById(newDTOs[i].gradeId()).orElse(null);
            assertEquals(g.getScore(), newScores[i]);
        }

        // Revert score changes.
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(result)))
                        .andReturn()
                        .getResponse();
        
        assertEquals(200, response.getStatus());
    }

    // Unit Test #5 - Instructor attempts to grade an assignment but the assignment id is invalid
    @Test
    public void gradeInvalidAssignment() throws Exception {
        MockHttpServletResponse response;

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/55/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        assertEquals(404, response.getStatus());
        assertEquals(response.getErrorMessage(), "Assignment 55 not found");
    }
}