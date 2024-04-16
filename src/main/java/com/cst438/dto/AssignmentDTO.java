
package com.cst438.dto;

import com.cst438.domain.Assignment;
import com.cst438.domain.Section;

/*
 * Data Transfer Object for assignment data
 */
public record AssignmentDTO(
    int id,
    String title,
    String dueDate,
    String courseId,
    int secId,
    int secNo)
{
    public static AssignmentDTO fromEntity(final Assignment assignment) {
        final Section section = assignment.getSection();
        return new AssignmentDTO(
            assignment.getAssignmentId(),
            assignment.getTitle(),
            assignment.getDueDate().toString(),
            section.getCourse().getCourseId(),
            section.getSecId(),
            section.getSectionNo()
        );
    }
}
