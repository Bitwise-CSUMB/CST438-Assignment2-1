package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.GradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    // instructor lists assignments for a section. Assignments ordered by due date.
    // logged in user must be the instructor for the section
    @GetMapping("/sections/{secNo}/assignments") // AssignmentsView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<AssignmentDTO> getAssignments(
        @PathVariable("secNo") final int secNo,
        final Principal principal)
    {
        // hint: use the assignment repository method findBySectionNoOrderByDueDate to return a list of assignments

        // validate user is the instructor of the section
        ControllerUtils.validateInstructorAndInstructorForSection(userRepository, principal, sectionRepository, secNo);

        // Fetch assignments from repository
        final List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);

        // Convert Assignment entities to DTOs
        return assignments.stream()
            .map(AssignmentDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // add assignment
    // user must be instructor of the section
    // return AssignmentDTO with assignmentID generated by database
    @PostMapping("/assignments") // AssignmentAdd.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public AssignmentDTO createAssignment(
        @RequestBody final AssignmentDTO assignmentDTO,
        final Principal principal)
    {
        // validate user is the instructor of the section
        final var validateRet = ControllerUtils.validateInstructorAndInstructorForSection(
            userRepository, principal, sectionRepository, assignmentDTO.secNo());

        final Assignment newAssignment = new Assignment();
        newAssignment.setSection(validateRet.section());

        // assignmentId generated by database
        return updateAssignmentTitleAndDueDate(newAssignment, assignmentDTO);
    }

    // update assignment for a section. Only title and dueDate may be changed.
    // user must be instructor of the section
    // return updated AssignmentDTO
    @PutMapping("/assignments") // AssignmentUpdate.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public AssignmentDTO updateAssignment(
        @RequestBody final AssignmentDTO assignmentDTO,
        final Principal principal)
    {
        // validate user is the instructor of the section
        ControllerUtils.validateInstructorAndInstructorForSection(userRepository,
            principal, sectionRepository, assignmentDTO.secNo());

        if (assignmentDTO.title().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment title not entered");
        }

        final Assignment assignment = assignmentRepository.findById(assignmentDTO.id()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found for ID: " + assignmentDTO.id()));

        return updateAssignmentTitleAndDueDate(assignment, assignmentDTO);
    }

    // delete assignment for a section
    // logged-in user must be instructor of the section
    @DeleteMapping("/assignments/{assignmentId}") // AssignmentsView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void deleteAssignment(
        @PathVariable("assignmentId") final int assignmentId,
        final Principal principal)
    {
        final Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);

        if (assignment == null) {
            return;
        }

        // validate user is the instructor of the section
        ControllerUtils.validateInstructorAndInstructorForSection(userRepository, principal, assignment.getSection());

        assignmentRepository.delete(assignment);
    }

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    @GetMapping("/assignments/{assignmentId}/grades") // AssignmentGrade.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<GradeDTO> getAssignmentGrades(
        @PathVariable("assignmentId") final int assignmentId,
        final Principal principal)
    {
        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method
        // findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        // hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        // if the grade does not exist, create a grade entity and set the score to NULL
        // and then save the new entity

        final Assignment assignment = assignmentRepository.findByAssignmentId(assignmentId);

        if (assignment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment " + assignmentId + " not found");
        }

        // validate user is the instructor of the section
        ControllerUtils.validateInstructorAndInstructorForSection(userRepository, principal, assignment.getSection());

        final List<Enrollment> enrollments = enrollmentRepository
            .findEnrollmentsBySectionNoOrderByStudentName(assignment.getSection().getSectionNo());

        final List<GradeDTO> grades = new ArrayList<>();
        for (final Enrollment enrollment : enrollments) {

            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(
                enrollment.getEnrollmentId(), assignment.getAssignmentId());

            if (grade == null) {
                grade = new Grade();
                grade.setAssignment(assignment);
                grade.setEnrollment(enrollment);
                grade = gradeRepository.save(grade);
            }

            grades.add(GradeDTO.fromEntity(grade));
        }

        return grades;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    @PutMapping("/grades") // AssignmentGrade.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateGrades(
        @RequestBody final List<GradeDTO> dlist,
        final Principal principal)
    {
        // validate user is an instructor
        final User user = ControllerUtils.validateInstructor(userRepository, principal);

        // for each grade in the GradeDTO list, retrieve the grade entity
        for (final GradeDTO gradeDTO : dlist) {

            // find the Grade entity
            final Grade grade = gradeRepository.findById(gradeDTO.gradeId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid gradeId: " + gradeDTO.gradeId()));

            // validate user is the instructor of the assignment's section
            ControllerUtils.validateInstructorForSection(user, grade.getAssignment().getSection());

            // update the score and save the entity
            grade.setScore(gradeDTO.score());
            gradeRepository.save(grade);
        }
    }

    // student lists their assignments/grades for an enrollment ordered by due date
    // student must be enrolled in the section
    @GetMapping("/assignments") // AssignmentsStudentView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<AssignmentStudentDTO> getStudentAssignments(
        @RequestParam("year") final int year,
        @RequestParam("semester") final String semester,
        final Principal principal)
    {
        // return a list of assignments and (if they exist) the assignment grade
        // for all sections that the student is enrolled for the given year and semester
        // hint: use the assignment repository method
        // findByStudentIdAndYearAndSemesterOrderByDueDate

        final int studentId = ControllerUtils.validateStudent(userRepository, principal).getId();

        final List<Assignment> assignments = assignmentRepository
            .findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);

        if (assignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No assignments found for studentId "
                + studentId + " in the year " + year + " in the semester " + semester);
        }

        final List<AssignmentStudentDTO> dtoList = new ArrayList<>();
        for (final Assignment assignment : assignments) {

            final Enrollment enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(
                assignment.getSection().getSectionNo(), studentId
            );
            final Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(
                enrollment.getEnrollmentId(), assignment.getAssignmentId());

            if (grade != null) {
                dtoList.add(AssignmentStudentDTO.fromGradeEntity(grade));
            }
            else {
                dtoList.add(AssignmentStudentDTO.fromAssignmentEntity(assignment));
            }
        }
        return dtoList;
    }

    // Note: Unused
//    @GetMapping("/allassignments")
//    public List<AssignmentDTO> getAllAssignments() {
//        return assignmentRepository.findAllAssignments().stream()
//            .map(AssignmentDTO::fromEntity)
//            .collect(Collectors.toList());
//    }

    private AssignmentDTO updateAssignmentTitleAndDueDate(
        final Assignment assignment,
        final AssignmentDTO assignmentDTO)
    {
        final java.sql.Date sqlDueDate = validateDueDate(assignment.getSection(), assignmentDTO);
        assignment.setTitle(assignmentDTO.title());
        assignment.setDueDate(sqlDueDate);
        return AssignmentDTO.fromEntity(assignmentRepository.save(assignment));
    }

    private static java.sql.Date parseDueDate(final AssignmentDTO assignmentDTO) {
        try {
            // Parse the string directly into a java.sql.Date object
            return java.sql.Date.valueOf(assignmentDTO.dueDate());
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid due date format");
        }
    }

    private static java.sql.Date validateDueDate(final Section section, final AssignmentDTO assignmentDTO) {

        // get the start and end date of the term associated with the assignment
        final java.sql.Date sqlDueDate = parseDueDate(assignmentDTO);
        final Term term = section.getTerm();
        final Date classStartDate = term.getStartDate();
        final Date classEndDate = term.getEndDate();

        // check if the due date is past the end date of the class
        if (sqlDueDate.before(classStartDate) || sqlDueDate.after(classEndDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad due date; section "
                + section.getSectionNo() + " timeframe: " + classStartDate + " - " + classEndDate);
        }
        return sqlDueDate;
    }
}
