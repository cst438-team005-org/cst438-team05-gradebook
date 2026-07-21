
package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.GradeDTO;
import com.cst438.dto.LoginDTO;
import com.cst438.service.RegistrarServiceProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GradeControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private RegistrarServiceProxy registrarService;

    @Test
    public void getAndUpdateAssignmentGrades() {
        int enrollmentId = 900001;
        Assignment assignment = null;
        Grade createdGrade = null;

        try {
            // The starter data contains instructor ted@csumb.edu, student
            // sam@csumb.edu, and section 1 taught by Ted.
            User student = userRepository.findByEmail("sam@csumb.edu");
            Section section = sectionRepository.findById(1).orElse(null);

            assertNotNull(student, "student test data is missing");
            assertNotNull(section, "section test data is missing");

            // Add Sam to section 1 for this test.
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrollmentId(enrollmentId);
            enrollment.setStudent(student);
            enrollment.setSection(section);
            enrollment.setGrade(null);
            enrollmentRepository.save(enrollment);

            // Create an assignment in Ted's section.
            assignment = new Assignment();
            assignment.setTitle("Grade Controller Test Assignment");
            assignment.setSection(section);
            assignment = assignmentRepository.save(assignment);

            // Login as the instructor and obtain a JWT.
            EntityExchangeResult<LoginDTO> loginResult = client.get()
                    .uri("/login")
                    .headers(headers -> headers.setBasicAuth("ted@csumb.edu", "ted"))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LoginDTO.class)
                    .returnResult();

            LoginDTO login = loginResult.getResponseBody();
            assertNotNull(login);
            assertNotNull(login.jwt());
            String jwt = login.jwt();

            // GET should return Sam and create a Grade entity with a null score.
            EntityExchangeResult<GradeDTO[]> getResult = client.get()
                    .uri("/assignments/{assignmentId}/grades", assignment.getAssignmentId())
                    .headers(headers -> headers.setBearerAuth(jwt))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(GradeDTO[].class)
                    .returnResult();

            GradeDTO[] grades = getResult.getResponseBody();
            assertNotNull(grades);
            assertEquals(1, grades.length);

            GradeDTO gradeDTO = grades[0];
            assertTrue(gradeDTO.gradeId() > 0);
            assertEquals("sam@csumb.edu", gradeDTO.studentEmail());
            assertEquals("Grade Controller Test Assignment", gradeDTO.assignmentTitle());
            assertEquals("cst489", gradeDTO.courseId());
            assertEquals(1, gradeDTO.sectionId());
            assertNull(gradeDTO.score());

            createdGrade = gradeRepository.findById(gradeDTO.gradeId()).orElse(null);
            assertNotNull(createdGrade, "GET did not create the missing Grade entity");
            assertNull(createdGrade.getScore());

            // PUT should update the score for that Grade entity.
            GradeDTO updateDTO = new GradeDTO(
                    gradeDTO.gradeId(),
                    gradeDTO.studentName(),
                    gradeDTO.studentEmail(),
                    gradeDTO.assignmentTitle(),
                    gradeDTO.courseId(),
                    gradeDTO.sectionId(),
                    92
            );

            client.put()
                    .uri("/grades")
                    .headers(headers -> headers.setBearerAuth(jwt))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(List.of(updateDTO))
                    .exchange()
                    .expectStatus().isOk();

            Grade updatedGrade = gradeRepository.findById(gradeDTO.gradeId()).orElse(null);
            assertNotNull(updatedGrade);
            assertEquals(92, updatedGrade.getScore());

        } finally {
            // Remove data created by this test so it can be run repeatedly.
            if (createdGrade != null) {
                gradeRepository.deleteById(createdGrade.getGradeId());
            }
            if (assignment != null) {
                assignmentRepository.deleteById(assignment.getAssignmentId());
            }
            if (enrollmentRepository.existsById(enrollmentId)) {
                enrollmentRepository.deleteById(enrollmentId);
            }
        }
    }
}
