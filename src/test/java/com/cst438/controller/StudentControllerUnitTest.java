
// Authored by Andi
// Covers Unit Tests #6, #7, #8, and #9
// 6. Student enrolls into a section
// 7. Student enrolls into a section, but fails because the student is already enrolled
// 8. Student enrolls into a section, but the section number is invalid
// 9. Student enrolls into a section, but it is past the add deadline

package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest
public class StudentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository userRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Unit Test #6 - Student enrolls into a section
    @Test
    void addEnrollment() throws Exception {

        int sectionNumber = 9;
        int studentId = 3;

        // Perform enrollment
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + sectionNumber + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Assert enrollment is successful
        assertEquals(200, response.getStatus());
        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);
        assertNotEquals(0, result.enrollmentId());
        assertNotNull(enrollmentRepository.findById(result.enrollmentId()).orElse(null));

        // Cleanup
        MockHttpServletResponse cleanupResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/enrollments/{eId}", result.enrollmentId()))
                .andReturn()
                .getResponse();
        assertEquals(200, cleanupResponse.getStatus());
        assertNull(enrollmentRepository.findById(result.enrollmentId()).orElse(null));
    }

    // Unit Test #7 - Student enrolls into a section, but fails because the student is already enrolled
    @Test
    public void addSectionFailsAlreadyEnrolled( ) throws Exception {

        // enrollment where already enrolled
        int sectionNumber = 10;
        int studentId = 3;

        MockHttpServletResponse response;

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + sectionNumber + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // response should be 400
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("studentId is already enrolled in sectionNo", message);
    }

    // Unit Test #8 - Student enrolls into a section, but the section number is invalid
    @Test
    public void addSectionFailsBadSecNo( ) throws Exception {

        // section number -1 does not exist
        int sectionNumber = -1;
        int studentId = 3;

        MockHttpServletResponse response;

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + sectionNumber + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // response should be 404
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Unknown sectionNo", message);

    }

    // Unit Test #9 - Student enrolls into a section, but it is past the add deadline
    @Test
    public void addSectionFailsDeadline( ) throws Exception {

        // add deadline 2022-01-30
        int sectionNumber = 5;
        int studentId = 3;

        MockHttpServletResponse response;

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + sectionNumber + "?studentId=" + studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // response should be 400
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Course cannot be added at this time", message);

    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
