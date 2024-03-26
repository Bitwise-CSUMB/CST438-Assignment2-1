package com.cst438.controller;

import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.SectionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
@SpringBootTest
public class StudentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    // student enrolls into a section, but fails because the student is already enrolled
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

    }

    // student enrolls into a section, but the section number is invalid
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

    }

    // student enrolls into a section, but it is past the add deadline
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

    }
}
