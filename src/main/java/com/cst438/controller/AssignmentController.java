package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.GradeDTO;
import com.cst438.dto.EnrollmentDTO;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    // instructor lists assignments for a section. Assignments ordered by due date.
    // logged in user must be the instructor for the section
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {

        // hint: use the assignment repository method
        // findBySectionNoOrderByDueDate to return
        // a list of assignments

        // Fetch assignments from repository
        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);

        if (assignments.isEmpty()) {
            return new ArrayList<AssignmentDTO>();
        }

        // Convert Assignment entities to DTOs
        return assignments.stream()
            .map(assignment -> new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate().toString(),
                assignment.getSection().getCourse().getCourseId(),
                assignment.getSection().getSecId(),
                assignment.getSection().getSectionNo()))
            .collect(Collectors.toList());
    }

    // add assignment
    // user must be instructor of the section
    // return AssignmentDTO with assignmentID generated by database
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO assignmentDTO) {

        Section s = sectionRepository.findById(assignmentDTO.secNo()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Section " + assignmentDTO.secNo() + " not found"));


        Assignment a = new Assignment();
        a.setSection(s);

        // assignmentId generated by database

        return getAssignmentDTO(assignmentDTO, a);
    }

    // update assignment for a section. Only title and dueDate may be changed.
    // user must be instructor of the section
    // return updated AssignmentDTO
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO assignmentDTO) {
        if (assignmentDTO.title().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment title not entered");
        }
        Assignment a = assignmentRepository.findById(assignmentDTO.id()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found for ID: " + assignmentDTO.id()));
        return getAssignmentDTO(assignmentDTO, a);
    }

    private AssignmentDTO getAssignmentDTO(
        @RequestBody AssignmentDTO assignmentDTO,
        Assignment a) {
        a.setTitle(assignmentDTO.title());

        java.sql.Date sqlDueDate;
        try {
            // Parse the string directly into a java.sql.Date object
            sqlDueDate = java.sql.Date.valueOf(assignmentDTO.dueDate());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid due date format");
        }
        a.setDueDate(sqlDueDate);

        assignmentRepository.save(a);

        return new AssignmentDTO(
            a.getAssignmentId(),
            a.getTitle(),
            a.getDueDate().toString(),
            a.getSection().getCourse().getCourseId(),
            a.getSection().getSecId(),
            a.getSection().getSectionNo()
        );
    }

    // delete assignment for a section
    // logged-in user must be instructor of the section
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {

        Assignment a = assignmentRepository.findByAssignmentId(assignmentId);
        assignmentRepository.delete(a);
    }

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {

        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method
        // findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        // hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        // if the grade does not exist, create a grade entity and set the score to NULL
        // and then save the new entity

        Assignment a = assignmentRepository.findByAssignmentId(assignmentId);

        if (a == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment " + assignmentId + " not found");
        }

        List<Enrollment> enrollments = enrollmentRepository
                .findEnrollmentsBySectionNoOrderByStudentName(a.getSection().getSectionNo());

        List<GradeDTO> grades = new ArrayList<GradeDTO>();
        for (Enrollment e : enrollments) {
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), a.getAssignmentId());
            Section s = a.getSection();
            Course c = s.getCourse();
            User u = e.getUser();

            if (g == null) {
                g = new Grade();
                g.setAssignment(a);
                g.setEnrollment(e);
                g.setScore(null);
                gradeRepository.save(g);
            }
            GradeDTO dto = new GradeDTO(g.getGradeId(), u.getName(), u.getEmail(),
                        a.getTitle(), c.getCourseId(), s.getSecId(), g.getScore());
            grades.add(dto);
        }

        return grades;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {

        // for each grade in the GradeDTO list, retrieve the grade entity
        for (GradeDTO gradeDTO : dlist) {
            // find the Grade entity
            Grade grade = null;
            Optional<Grade> optionalGrade = gradeRepository.findById(gradeDTO.gradeId());
            if (optionalGrade.isPresent()) {
                grade = optionalGrade.get();
                // update the score and save the entity
                grade.setScore(gradeDTO.score());
                gradeRepository.save(grade);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Invalid gradeId: " + gradeDTO.gradeId());
            }
        }

    }

    // student lists their assignments/grades for an enrollment ordered by due date
    // student must be enrolled in the section
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        // return a list of assignments and (if they exist) the assignment grade
        // for all sections that the student is enrolled for the given year and semester
        // hint: use the assignment repository method
        // findByStudentIdAndYearAndSemesterOrderByDueDate

        List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        if (assignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No assignments found for studentId " + studentId
            + " in the year " + year + " in the semester " + semester);
        }
        List<AssignmentStudentDTO> dto_list = new ArrayList<>();
        for (Assignment a : assignments) {
            Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(a.getSection().getSectionNo(), studentId);
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), a.getAssignmentId());
            if (g != null) {
                dto_list.add(new AssignmentStudentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    g.getScore()
                ));
            } else {
                dto_list.add(new AssignmentStudentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    null
                ));
            }
        }
        return dto_list;
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
