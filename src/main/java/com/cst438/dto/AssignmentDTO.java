package com.cst438.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * Data Transfer Object for assignment data
 */
public record AssignmentDTO(
        int id,
        @NotNull(message="title required")
        @NotBlank(message="tile cannot be blank")
        @Size(max=250, message="title max length 250")
        @Pattern(regexp ="^[a-zA-Z0-9., ]+$", message="invalid char in title")
        String title,
        String dueDate,
        String courseId,
        int secId,
        int secNo

) {
}
