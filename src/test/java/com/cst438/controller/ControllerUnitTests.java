package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.*;
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
 * Unit tests for 4 and 5
 */

@AutoConfigureMockMvc
@SpringBootTest
public class ControllerUnitTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentController enrollmentController;

    // Unit test to grade assignment
    // Unit Test 4.
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

            int newScore = oldScores[i] - 10;
            if (newScore < 0)
                newScore += 100;

            newScores[i] = newScore;
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

    // Unit test to grade invalid assignment.
    // Unit Test 5.
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
