package com.cst438.dto;

/*
 * Data Transfer Object for data for a section of a course
 */
public record SectionDTO(
    int secNo,
    int year,               // Expansion of termId (not in dto)
    String semester,        // Expansion of termId (not in dto)
    String courseId,
    int secId,
    String building,
    String room,
    String times,
    String instructorName,  // Expansion of instructorEmail
    String instructorEmail
)
{

}
