package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.SectionDTO;
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

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class SectionControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Test
    public void addSection() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new section.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the section is inserted.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst499",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert section to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                        .andReturn()
                        .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        SectionDTO result = fromJsonString(response.getContentAsString(), SectionDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.secNo());
        // check other fields of the DTO for expected values
        assertEquals("cst499", result.courseId());

        // check the database
        Section s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNotNull(s);
        assertEquals("cst499", s.getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/sections/"+result.secNo()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        s = sectionRepository.findById(result.secNo()).orElse(null);
        assertNull(s);  // section should not be found after delete
    }

    @Test
    public void addSectionFailsBadCourse( ) throws Exception {

        MockHttpServletResponse response;

        // course id cst599 does not exist.
        SectionDTO section = new SectionDTO(
                0,
                2024,
                "Spring",
                "cst599",
                1,
                "052",
                "104",
                "W F 1:00-2:50 pm",
                "Joshua Gross",
                "jgross@csumb.edu"
        );

        // issue the POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/sections")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(section)))
                .andReturn()
                .getResponse();

        // response should be 404, NOT_FOUND
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("course not found cst599", message);

    }

    // Instructor adds a new assignment successfully
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
        assertEquals("2024-03-01", result.dueDate().toString());
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

    // Instructor adds a new assignment with a due date past the end date of the class
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

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
