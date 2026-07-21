package com.cst438.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * Data Transfer Object for student enrollment data for a section of a course
 */
public record EnrollmentDTO(

        int enrollmentId,
        @Size(max=5, message="grade max length 5")
        @Pattern(regexp ="^[a-zA-Z+-]+$", message="invalid char in grade")
        String grade,  // final grade. May be null until instructor enters final grades.
        int studentId,
        String name,
        String email,
        String courseId,
        String title,
        int sectionId,
        int sectionNo,
        String building,
        String room,
        String times,
        int credits,
        int year,
        String semester

) {
}
