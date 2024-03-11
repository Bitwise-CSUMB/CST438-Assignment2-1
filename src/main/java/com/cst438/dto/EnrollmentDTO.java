package com.cst438.dto;

import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.Section;
import com.cst438.domain.Term;
import com.cst438.domain.User;

/*
 * Data Transfer Object for student enrollment data for a section of a course
 */
public record EnrollmentDTO(
    int enrollmentId,
    String grade,  // final grade. May be null until instructor enters final grades.
    int studentId,
    String name,
    String email,
    String courseId,
    int sectionId,
    int sectionNo,
    String building,
    String room,
    String times,
    int credits,
    int year,
    String semester)
{
    public static EnrollmentDTO fromEntity(Enrollment enrollment) {

        User user = enrollment.getUser();
        Section section = enrollment.getSection();
        Course course = section.getCourse();
        Term term = section.getTerm();

        return new EnrollmentDTO(
            enrollment.getEnrollmentId(),
            enrollment.getGrade(),
            user.getId(),
            user.getName(),
            user.getEmail(),
            course.getCourseId(),
            section.getSecId(),
            section.getSectionNo(),
            section.getBuilding(),
            section.getRoom(),
            section.getTimes(),
            course.getCredits(),
            term.getYear(),
            term.getSemester()
        );
    }
}
