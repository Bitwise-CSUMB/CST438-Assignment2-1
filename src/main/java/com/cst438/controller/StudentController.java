
package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    // student gets transcript showing list of all enrollments
    // example URL   /transcripts
    @GetMapping("/transcripts") // Transcript.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(
        final Principal principal)
    {
        // list course_id, sec_id, title, credit, grade in chronological order
        // user must be a student
        // hint: use enrollment repository method findEnrollmentByStudentIdOrderByTermId

        final User user = ControllerUtils.validateStudent(userRepository, principal);
        final int studentId = user.getId();

        return enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId)
            .stream()
            .map(EnrollmentDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // student gets a list of their enrollments for the given year, semester
    // user must be student
    @GetMapping("/enrollments") // ScheduleView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getSchedule(
       @RequestParam("year") final int year,
       @RequestParam("semester") final String semester,
       final Principal principal)
    {
        // hint: use enrollment repository method findByYearAndSemesterOrderByCourseId

        final User user = ControllerUtils.validateStudent(userRepository, principal);
        final int studentId = user.getId();

        return enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId)
            .stream()
            .map(EnrollmentDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // student adds enrollment into a section
    // user must be student
    // return EnrollmentDTO with enrollmentId generated by database
    @PostMapping("/enrollments/sections/{sectionNo}") // CourseEnroll.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(
        @PathVariable final int sectionNo,
        final Principal principal)
    {
        // check that the Section entity with primary key sectionNo exists
        // check that today is between addDate and addDeadline for the section
        // check that student is not already enrolled into this section
        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.

        final User user = ControllerUtils.validateStudent(userRepository, principal);
        final int studentId = user.getId();

        final Section section = sectionRepository.findById(sectionNo).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown sectionNo"));

        final Term term = section.getTerm();
        final Date now = Date.from(Instant.now());

        if (now.before(term.getAddDate()) || now.after(term.getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course cannot be added at this time");
        }

        if (enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already enrolled in sectionNo");
        }

        final Enrollment newEnrollment = new Enrollment();
        newEnrollment.setUser(user);
        newEnrollment.setSection(section);
        enrollmentRepository.save(newEnrollment);

        return EnrollmentDTO.fromEntity(newEnrollment);
    }

    // student drops a course
    // user must be student
    @DeleteMapping("/enrollments/{enrollmentId}") // ScheduleView.js
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(
        @PathVariable("enrollmentId") final int enrollmentId,
        final Principal principal)
    {
        final Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown enrollmentId"));

        final User enrollmentUser = enrollment.getUser();

        if (!principal.getName().equals(enrollmentUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempted to drop another user's enrollment");
        }

        if (!enrollmentUser.getType().equals("STUDENT")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only students can drop a course");
        }

        // check that today is not after the dropDeadline for section
        final Date dropDeadline = enrollment.getSection().getTerm().getDropDeadline();
        if (Date.from(Instant.now()).after(dropDeadline)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drop deadline has been exceeded");
        }

        enrollmentRepository.delete(enrollment);
    }
}
