
package com.cst438.dto;

import com.cst438.domain.Assignment;
import com.cst438.domain.Grade;
import com.cst438.domain.Section;

import java.sql.Date;

/*
 * Data Transfer Object for assignment data including student's grade
 */
public record AssignmentStudentDTO(
    int assignmentId,
    String title,
    Date dueDate,
    String courseId,
    int sectionId,
    Integer score)
{
    public static AssignmentStudentDTO fromGradeEntity(final Grade grade) {
        final Assignment assignment = grade.getAssignment();
        final Section section = assignment.getSection();
        return new AssignmentStudentDTO(
            assignment.getAssignmentId(),
            assignment.getTitle(),
            assignment.getDueDate(),
            section.getCourse().getCourseId(),
            section.getSecId(),
            grade.getScore()
        );
    }

    public static AssignmentStudentDTO fromAssignmentEntity(final Assignment assignment) {
        final Section section = assignment.getSection();
        return new AssignmentStudentDTO(
            assignment.getAssignmentId(),
            assignment.getTitle(),
            assignment.getDueDate(),
            section.getCourse().getCourseId(),
            section.getSecId(),
            null
        );
    }
}
