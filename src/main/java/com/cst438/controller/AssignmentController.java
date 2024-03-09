package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.GradeDTO;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    // instructor lists assignments for a section. Assignments ordered by due date.
    // logged in user must be the instructor for the section
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {

        // TODO remove the following line when done

        // hint: use the assignment repository method
        // findBySectionNoOrderByDueDate to return
        // a list of assignments

        return null;
    }

    // add assignment
    // user must be instructor of the section
    // return AssignmentDTO with assignmentID generated by database
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) {

        // TODO remove the following line when done

        return null;
    }

    // update assignment for a section. Only title and dueDate may be changed.
    // user must be instructor of the section
    // return updated AssignmentDTO
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {

        // TODO remove the following line when done

        return null;
    }

    // delete assignment for a section
    // logged in user must be instructor of the section
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {

        // TODO
    }

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {

        // TODO remove the following line when done
        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method
        // findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        // hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        // if the grade does not exist, create a grade entity and set the score to NULL
        // and then save the new entity

        Assignment a = assignmentRepository.findByAssignmentId(assignmentId);
        List<Enrollment> enrollments = enrollmentRepository
                .findEnrollmentsBySectionNoOrderByStudentName(a.getSection().getSectionNo());

        List<GradeDTO> grades = new ArrayList<GradeDTO>();
        for (Enrollment e : enrollments) {
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), a.getAssignmentId());
            Section s = a.getSection();
            Course c = s.getCourse();
            User u = e.getUser();

            if (g != null) {
                grades.add(new GradeDTO(g.getGradeId(), u.getName(), u.getEmail(),
                        a.getTitle(), c.getCourseId(), s.getSecId(), g.getScore()));
            } else {
                grades.add(new GradeDTO(new Grade().getGradeId(), u.getName(), u.getEmail(),
                        a.getTitle(), c.getCourseId(), s.getSecId(), null));
            }
        }

        return grades;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {

        // TODO

        // for each grade in the GradeDTO list, retrieve the grade entity
        // update the score and save the entity

    }

    // student lists their assignments/grades for an enrollment ordered by due date
    // student must be enrolled in the section
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        // TODO remove the following line when done

        // return a list of assignments and (if they exist) the assignment grade
        // for all sections that the student is enrolled for the given year and semester
        // hint: use the assignment repository method
        // findByStudentIdAndYearAndSemesterOrderByDueDate

        return null;
    }

    @GetMapping("/allassignments")
    public List<AssignmentDTO> getAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAllAssignments();
        List<AssignmentDTO> dto_list = new ArrayList<>();
        for (Assignment a : assignments) {
            dto_list.add(new AssignmentDTO(a.getAssignmentId(), a.getTitle(), a.getDueDate().toString(),
                    a.getSection().getCourse().getCourseId(), a.getSection().getSecId(),
                    a.getSection().getSectionNo()));
        }
        return dto_list;
    }
}
