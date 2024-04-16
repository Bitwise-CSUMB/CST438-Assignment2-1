
package com.cst438.dto;

import com.cst438.domain.Enrollment;
import com.cst438.domain.Grade;
import com.cst438.domain.Section;
import com.cst438.domain.User;

/*
 * Data Transfer Object for student's score for an assignment
 */
public record GradeDTO(
    int gradeId,
    String studentName,
    String studentEmail,
    String assignmentTitle,
    String courseId,
    int sectionId,
    Integer score)
{
    public static GradeDTO fromEntity(final Grade grade) {
        final Enrollment enrollment = grade.getEnrollment();
        final User user = enrollment.getUser();
        final Section section = enrollment.getSection();
        return new GradeDTO(
            grade.getGradeId(),
            user.getName(),
            user.getEmail(),
            grade.getAssignment().getTitle(),
            section.getCourse().getCourseId(),
            section.getSecId(),
            grade.getScore()
        );
    }
}
