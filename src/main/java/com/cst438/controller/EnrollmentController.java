package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.RegistrarServiceProxy;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final RegistrarServiceProxy registrar;

    public EnrollmentController (
            EnrollmentRepository enrollmentRepository,
            SectionRepository sectionRepository,
            RegistrarServiceProxy registrar
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
        this.registrar = registrar;
    }


    // instructor gets student enrollments with grades for a section
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo, Principal principal ) {

        // check that the sectionNo belongs to the logged in instructor.
        Section section = sectionRepository.findById(sectionNo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "section not found " + sectionNo));

        if (!section.getInstructorEmail().equals(principal.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "not instructor for this section " + sectionNo);
        }

        // use the EnrollmentRepository findEnrollmentsBySectionNoOrderByStudentName
        // to get a list of Enrollments for the given sectionNo.
        // Return a list of EnrollmentDTOs
        List<Enrollment> enrollments =
                enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        List<EnrollmentDTO> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            result.add(toDTO(enrollment, section));
        }
        return result;
    }

    // instructor updates enrollment grades
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@Valid @RequestBody List<EnrollmentDTO> dtoList, Principal principal) {
        // for each EnrollmentDTO
        //    check that logged in user is instructor for the section
        //    update the enrollment grade
        //    send message to Registrar service for grade update
        for (EnrollmentDTO dto : dtoList) {
            Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "enrollment not found " + dto.enrollmentId()));

            Section section = enrollment.getSection();
            if (!section.getInstructorEmail().equals(principal.getName())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "not instructor for this section " + section.getSectionNo());
            }

            enrollment.setGrade(dto.grade());
            enrollmentRepository.save(enrollment);

            // notify Registrar service so the grade also appears on the
            // student's transcript there (Registrar has no shared database).
            registrar.sendMessage("updateEnrollment", toDTO(enrollment, section));
        }
    }

    private EnrollmentDTO toDTO(Enrollment enrollment, Section section) {
        return new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                enrollment.getGrade(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getStudent().getEmail(),
                section.getCourse().getCourseId(),
                section.getCourse().getTitle(),
                section.getSectionId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                section.getCourse().getCredits(),
                section.getTerm().getYear(),
                section.getTerm().getSemester()
        );
    }
}
