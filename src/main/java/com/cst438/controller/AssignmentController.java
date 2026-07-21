package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.SectionDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AssignmentController {

  private final SectionRepository sectionRepository;
  private final AssignmentRepository assignmentRepository;
  private final GradeRepository gradeRepository;
  private final UserRepository userRepository;

  public AssignmentController(
      SectionRepository sectionRepository,
      AssignmentRepository assignmentRepository,
      GradeRepository gradeRepository,
      UserRepository userRepository
  ) {
    this.sectionRepository = sectionRepository;
    this.assignmentRepository = assignmentRepository;
    this.gradeRepository = gradeRepository;
    this.userRepository = userRepository;
  }

  // get Sections for an instructor
  @GetMapping("/sections")
  @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
  public List<SectionDTO> getSectionsForInstructor(
      @RequestParam("year") int year,
      @RequestParam("semester") String semester,
      Principal principal) {
    String instructorEmail = principal.getName();

    List<Section> sections = sectionRepository.findByInstructorEmailAndYearAndSemester(
        instructorEmail, year, semester);
    List<SectionDTO> sectionDTOs = new ArrayList<>();

    // convert Section entities to SectionDTOs
    for (Section section : sections) {
      User instructor = userRepository.findByEmail(section.getInstructorEmail());
      SectionDTO dto = new SectionDTO(
          section.getSectionNo(),
          section.getTerm().getYear(),
          section.getTerm().getSemester(),
          section.getCourse().getCourseId(),
          section.getCourse().getTitle(),
          section.getSectionId(),
          section.getBuilding(),
          section.getRoom(),
          section.getTimes(),
          instructor.getName(),
          section.getInstructorEmail()
      );
      sectionDTOs.add(dto);
    }
    // return the Sections that have instructorEmail for the
    // logged in instructor user for the given term.
    return sectionDTOs;
  }

  // instructor lists assignments for a section.
  @GetMapping("/sections/{secNo}/assignments")
  @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
  public List<AssignmentDTO> getAssignments(
      @PathVariable("secNo") int secNo,
      Principal principal) {

    Section section = sectionRepository.findById(secNo)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

    if (!section.getInstructorEmail().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You are not the instructor for this section");
    }
    List<Assignment> assignments = section.getAssignments();
    List<AssignmentDTO> assignmentDTOs = new ArrayList<>();

    for (Assignment assignment : assignments) {
      AssignmentDTO dto = new AssignmentDTO(
          assignment.getAssignmentId(),
          assignment.getTitle(),
          assignment.getDueDate().toString(),
          section.getCourse().getCourseId(),
          section.getSectionId(),
          section.getSectionNo()
      );
      assignmentDTOs.add(dto);
    }
    // verify that user is the instructor for the section
    //  return list of assignments for the Section
    return assignmentDTOs;
  }


  @PostMapping("/assignments")
  @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
  public AssignmentDTO createAssignment(
      @Valid @RequestBody AssignmentDTO dto,
      Principal principal) {

    Section section = sectionRepository.findById(dto.secNo())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

    if (!section.getInstructorEmail().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You are not the instructor for this section");
    }

    Date dueDate;
    try {
      dueDate = Date.valueOf(dto.dueDate());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid due date");
    }

    if (dueDate.before(section.getTerm().getStartDate()) || dueDate.after(section.getTerm().getEndDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be within the term");
    }

    Assignment assignment = new Assignment();
    assignment.setTitle(dto.title());
    assignment.setDueDate(dueDate);
    assignment.setSection(section);
    Assignment savedAssignment = assignmentRepository.save(assignment);
    //  user must be the instructor for the Section
    //  check that assignment dueDate is between start date and
    //  end date of the term
    //  create and save an Assignment entity
    //  return AssignmentDTO with database generated primary key
    return new AssignmentDTO(
        savedAssignment.getAssignmentId(),
        savedAssignment.getTitle(),
        savedAssignment.getDueDate().toString(),
        section.getCourse().getCourseId(),
        section.getSectionId(),
        section.getSectionNo()
    );
  }


  @PutMapping("/assignments")
  @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
  public AssignmentDTO updateAssignment(@Valid @RequestBody AssignmentDTO dto,
      Principal principal) {

    Assignment assignment = assignmentRepository.findById(dto.id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

    Section section = assignment.getSection();

    if (!section.getInstructorEmail().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You are not the instructor for this section");
    }

    Date dueDate;

    try {
      dueDate = Date.valueOf(dto.dueDate());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid due date");
    }

    if (dueDate.before(section.getTerm().getStartDate()) || dueDate.after(section.getTerm().getEndDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be within the term");
    }

    assignment.setTitle(dto.title());
    assignment.setDueDate(dueDate);

    Assignment savedAssignment = assignmentRepository.save(assignment);
    //  update Assignment Entity.  only title and dueDate fields can be changed.
    //  user must be instructor of the Section
    return new AssignmentDTO(
        savedAssignment.getAssignmentId(),
        savedAssignment.getTitle(),
        savedAssignment.getDueDate().toString(),
        section.getCourse().getCourseId(),
        section.getSectionId(),
        section.getSectionNo()
    );
  }


  @DeleteMapping("/assignments/{assignmentId}")
  @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
  public void deleteAssignment(@PathVariable("assignmentId") int assignmentId,
      Principal principal) {

    Assignment assignment = assignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

    Section section = assignment.getSection();

    if (!section.getInstructorEmail().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "You are not the instructor for this section");
    }
    assignmentRepository.delete(assignment);
    // verify that user is the instructor of the section
    // delete the Assignment entity
  }

  // student lists their assignments/grades  ordered by due date
  @GetMapping("/assignments")
  @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
  public List<AssignmentStudentDTO> getStudentAssignments(
      @RequestParam("year") int year,
      @RequestParam("semester") String semester,
      Principal principal) {

    String studentEmail = principal.getName();
    List<Assignment> assignments = assignmentRepository.findByStudentEmailAndYearAndSemester(studentEmail, year, semester);
    List<AssignmentStudentDTO> assignmentDTOs = new ArrayList<>();

    for (Assignment assignment : assignments) {
      Grade grade = gradeRepository.findByStudentEmailAndAssignmentId(studentEmail, assignment.getAssignmentId());
      Integer score = null;
      if (grade != null) {
        score = grade.getScore();
      }
      Section section = assignment.getSection();
      AssignmentStudentDTO dto = new AssignmentStudentDTO(
          assignment.getAssignmentId(),
          assignment.getTitle(),
          assignment.getDueDate(),
          section.getCourse().getCourseId(),
          section.getSectionId(),
          score
      );
      assignmentDTOs.add(dto);
    }
    //  return AssignmentStudentDTOs with scores of a
    //  Grade entity exists.
    //  hint: use the GradeRepository findByStudentEmailAndAssignmentId
    //  If assignment has not been graded, return a null score.
    return assignmentDTOs;
  }
}
