package com.cst438.controller;


import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo,
            EnrollmentRepository enrollmentRepository) {

        // TODO
        //  hint: use enrollment repository findEnrollmentsBySectionNoOrderByStudentName method

        // Fetch enrollments from repository
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        // Convert Enrollment entities to DTOs
        return enrollments.stream()
                .map(enrollment -> new EnrollmentDTO(
                        enrollment.getEnrollmentId(),
                        enrollment.getGrade(),
                        enrollment.getStudent().getId(),
                        enrollment.getStudent().getName(),
                        enrollment.getStudent().getEmail(),
                        enrollment.getSection().getCourse().getCourseId(),
                        enrollment.getSection().getSecId(),
                        enrollment.getSection().getSectionNo(),
                        enrollment.getSection().getBuilding(),
                        enrollment.getSection().getRoom(),
                        enrollment.getSection().getTimes(),
                        enrollment.getSection().getCourse().getCredits(),
                        enrollment.getSection().getTerm().getYear(),
                        enrollment.getSection().getTerm().getSemester()
                ))
                .collect(Collectors.toList());
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist,
                                      EnrollmentRepository enrollmentRepository) {

        // TODO
        // For each EnrollmentDTO in the list
        for (EnrollmentDTO enrollmentDTO : dlist) {
            //  find the Enrollment entity using enrollmentId
            Enrollment enrollment = null;
            Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(enrollment.getEnrollmentId());
            if (optionalEnrollment.isPresent()) {
                enrollment = optionalEnrollment.get();
                //  update the grade and save back to database
                enrollment.setGrade(enrollment.getGrade());
                enrollmentRepository.save(enrollment);
            }
        }
    }

}