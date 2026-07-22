package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.LoginDTO;
import com.cst438.dto.SectionDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AssignmentControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void assignmentControllerMethods() {

        String instructorJwt = login("ted@csumb.edu", "ted2025");
        String studentJwt = login("sam@csumb.edu", "sam2025");

        User student = userRepository.findByEmail("sam@csumb.edu");
        Section section = sectionRepository.findById(1).orElse(null);

        assertNotNull(student);
        assertNotNull(section);

        List<Enrollment> sectionEnrollments =
            enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(
                section.getSectionNo()
            );

        Enrollment enrollment = null;

        for (Enrollment existingEnrollment : sectionEnrollments) {
            if (existingEnrollment.getStudent().getId() == student.getId()) {
                enrollment = existingEnrollment;
                break;
            }
        }

        boolean createdEnrollment = false;

        if (enrollment == null) {
            enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setSection(section);
            enrollment.setGrade(null);
            enrollment = enrollmentRepository.save(enrollment);
            createdEnrollment = true;
        }

        client.get()
                .uri("/sections?year=2026&semester=Fall")
                .headers(headers -> headers.setBearerAuth(instructorJwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SectionDTO.class)
                .value(sections -> {
                    assertEquals(1, sections.size());
                    assertEquals(1, sections.get(0).secNo());
                });

        AssignmentDTO createDTO = new AssignmentDTO(
                0,
                "Unit Test Assignment",
                "2026-10-15",
                null,
                0,
                1
        );

        EntityExchangeResult<AssignmentDTO> createResult =
                client.post()
                        .uri("/assignments")
                        .headers(headers -> headers.setBearerAuth(instructorJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createDTO)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(AssignmentDTO.class)
                        .returnResult();

        AssignmentDTO created = createResult.getResponseBody();
        assertNotNull(created);
        assertTrue(created.id() > 0);

        client.get()
                .uri("/sections/1/assignments")
                .headers(headers -> headers.setBearerAuth(instructorJwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssignmentDTO.class)
                .value(assignments ->
                        assertTrue(assignments.stream()
                                .anyMatch(a -> a.id() == created.id())));

        AssignmentDTO updateDTO = new AssignmentDTO(
                created.id(),
                "Updated Unit Test Assignment",
                "2026-11-01",
                null,
                0,
                1
        );

        client.put()
                .uri("/assignments")
                .headers(headers -> headers.setBearerAuth(instructorJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AssignmentDTO.class)
                .value(updated ->
                        assertEquals("Updated Unit Test Assignment",
                                updated.title()));

        client.get()
                .uri("/assignments?year=2026&semester=Fall")
                .headers(headers -> headers.setBearerAuth(studentJwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssignmentStudentDTO.class)
                .value(assignments ->
                        assertTrue(assignments.stream()
                                .anyMatch(a -> a.assignmentId() == created.id())));

        client.delete()
                .uri("/assignments/" + created.id())
                .headers(headers -> headers.setBearerAuth(instructorJwt))
                .exchange()
                .expectStatus().isOk();

        Assignment deleted =
                assignmentRepository.findById(created.id()).orElse(null);
      assertNull(deleted);

        if (createdEnrollment) {
            enrollmentRepository.delete(enrollment);
        }
    }

    private String login(String email, String password) {

        EntityExchangeResult<LoginDTO> loginResult =
                client.get().uri("/login")
                        .headers(headers ->
                                headers.setBasicAuth(email, password))
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(LoginDTO.class)
                        .returnResult();

        LoginDTO loginDTO = loginResult.getResponseBody();
        assertNotNull(loginDTO);
        assertNotNull(loginDTO.jwt());

        return loginDTO.jwt();
    }
}
