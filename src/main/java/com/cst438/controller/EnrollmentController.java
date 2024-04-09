package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.RegistrarServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    private RegistrarServiceProxy registrarServiceProxy;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // instructor downloads student enrollments for a section, ordered by student name
    // user must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(@PathVariable("sectionNo") int sectionNo) {

        // Fetch enrollments from repository
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        // Convert Enrollment entities to DTOs
        return enrollments.stream().map(EnrollmentDTO::fromEntity).collect(Collectors.toList());
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {

        // For each EnrollmentDTO in the list
        for (EnrollmentDTO enrollmentDTO : dlist) {

            // find the Enrollment entity using enrollmentId
            Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(enrollmentDTO.enrollmentId());

            if (optionalEnrollment.isPresent()) {

                Enrollment enrollment = optionalEnrollment.get();

                // update the grade and save back to database
                enrollment.setGrade(enrollmentDTO.grade());
                enrollmentRepository.save(enrollment);

                // mirror the change in the registrar service
                registrarServiceProxy.sendUpdateEnrollment(enrollmentDTO);
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid enrollment "
                    + enrollmentDTO.enrollmentId());
            }
        }
    }
}
