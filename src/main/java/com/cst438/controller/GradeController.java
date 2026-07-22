package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.GradeDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GradeController {
    private final AssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;

    public GradeController(
            AssignmentRepository assignmentRepository,
            GradeRepository gradeRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.gradeRepository = gradeRepository;
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @GetMapping("/assignments/{assignmentId}/grades")
    @Transactional
    public List<GradeDTO> getAssignmentGrades(
            @PathVariable("assignmentId") int assignmentId,
            Principal principal) {

        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignment not found");
        }

        // An instructor may only view grades for an assignment in their own section.
        if (!assignment.getSection().getInstructorEmail().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not instructor for section");
        }

        // Index existing Grade entities by enrollment so that repeated GET requests do not
        // create duplicate grade rows.
        Map<Integer, Grade> gradesByEnrollment = new HashMap<>();
        for (Grade grade : assignment.getGrades()) {
            gradesByEnrollment.put(grade.getEnrollment().getEnrollmentId(), grade);
        }

        return assignment.getSection().getEnrollments().stream()
                .map(enrollment -> {
                    Grade grade = gradesByEnrollment.get(enrollment.getEnrollmentId());

                    // Every enrolled student needs a Grade row for this assignment.  The
                    // score remains null until the instructor enters it.
                    if (grade == null) {
                        grade = new Grade();
                        grade.setAssignment(assignment);
                        grade.setEnrollment(enrollment);
                        grade.setScore(null);
                        grade = gradeRepository.save(grade);
                    }

                    return toDTO(grade);
                })
                .toList();
    }

    @PutMapping("/grades")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @Transactional
    public void updateGrades(
            @Valid @RequestBody List<GradeDTO> dtoList,
            Principal principal) {

        for (GradeDTO dto : dtoList) {
            Grade grade = gradeRepository.findById(dto.gradeId()).orElse(null);
            if (grade == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade not found");
            }

            // Use the relationships from the database rather than trusting section/course
            // information supplied by the client.
            if (!grade.getAssignment().getSection().getInstructorEmail().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not instructor for section");
            }

            grade.setScore(dto.score());
            gradeRepository.save(grade);
        }
    }

    private GradeDTO toDTO(Grade grade) {
        Enrollment enrollment = grade.getEnrollment();
        Assignment assignment = grade.getAssignment();

        return new GradeDTO(
                grade.getGradeId(),
                enrollment.getStudent().getName(),
                enrollment.getStudent().getEmail(),
                assignment.getTitle(),
                assignment.getSection().getCourse().getCourseId(),
                assignment.getSection().getSectionId(),
                grade.getScore()
        );
    }
}
