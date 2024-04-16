package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments") // EnrollmentsView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<EnrollmentDTO> getEnrollments(
        @PathVariable("sectionNo") final int sectionNo,
        final Principal principal)
    {
        // hint: use enrollment repository findEnrollmentsBySectionNoOrderByStudentName method

        // validate user is the instructor of the section
        ControllerUtils.validateInstructorAndInstructorForSection(userRepository,
            principal, sectionRepository, sectionNo);

        // fetch enrollments from repository
        final List<Enrollment> enrollments = enrollmentRepository
            .findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        // convert Enrollment entities to DTOs
        return enrollments.stream().map(EnrollmentDTO::fromEntity).collect(Collectors.toList());
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments") // EnrollmentsView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateEnrollmentGrade(
        @RequestBody final List<EnrollmentDTO> dlist,
        final Principal principal)
    {
        // validate user is an instructor
        final User user = ControllerUtils.validateInstructor(userRepository, principal);

        // for each EnrollmentDTO in the list
        for (final EnrollmentDTO enrollmentDTO : dlist) {

            // find the Enrollment entity using enrollmentId
            final Enrollment enrollment = enrollmentRepository.findById(enrollmentDTO.enrollmentId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid enrollment " + enrollmentDTO.enrollmentId()));

            // validate user is the instructor of the section
            ControllerUtils.validateInstructorForSection(user, enrollment.getSection());

            // update the grade and save back to database
            enrollment.setGrade(enrollmentDTO.grade());
            enrollmentRepository.save(enrollment);
        }
    }
}
