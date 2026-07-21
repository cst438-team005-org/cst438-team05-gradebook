package com.cst438.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/*
 * Data Transfer Object for course data
 */
public record CourseDTO(

        String courseId,
        String title,
        int credits
) {
}
