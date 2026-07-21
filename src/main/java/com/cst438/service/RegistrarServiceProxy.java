package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RegistrarServiceProxy {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    TermRepository termRepository;


    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message) {

        System.out.println("received " + message);
        try {
            String[] parts = message.split(" ", 2);
            switch (parts[0]) {
                case "addCourse":
                case "updateCourse":
                    CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                    Course c = new Course();
                    c.setCourseId(dto.courseId());
                    c.setTitle(dto.title());
                    c.setCredits(dto.credits());
                    courseRepository.save(c);
                    break;
                case "deleteCourse":
                    courseRepository.deleteById(parts[1]);
                    break;

                case "addSection":
                case "updateSection":
                    SectionDTO sto = fromJsonString(parts[1], SectionDTO.class);
                    Section s = new Section();
                    s.setInstructorEmail(sto.instructorEmail());
                    s.setSectionId(sto.secId());
                    s.setBuilding(sto.building());
                    s.setRoom(sto.room());
                    s.setTimes(sto.times());
                    s.setSectionNo(sto.secNo());
                    Term t = termRepository.findByYearAndSemester(sto.year(), sto.semester());
                    s.setTerm(t);
                    Course co = courseRepository.findById(sto.courseId()).orElse(null);
                    s.setCourse(co);
                    sectionRepository.save(s);
                    break;

                case "deleteSection":
                    sectionRepository.deleteById(Integer.parseInt(parts[1]));
                    break;

                case "addUser":
                case "updateUser":
                    UserPasswordDTO uto = fromJsonString(parts[1], UserPasswordDTO.class);
                    User u = new User();
                    u.setId(uto.id());
                    u.setName(uto.name());
                    u.setEmail(uto.email());
                    u.setPassword(uto.password());
                    u.setType(uto.type());
                    userRepository.save(u);
                    break;
                case "deleteUser":
                    userRepository.deleteById(Integer.parseInt(parts[1]));
                    break;

                case "addEnrollment":
                    EnrollmentDTO eto = fromJsonString(parts[1], EnrollmentDTO.class);
                    Enrollment e = new Enrollment();
                    e.setEnrollmentId(eto.enrollmentId());
                    User student = userRepository.findById(eto.studentId()).orElse(null);
                    e.setStudent(student);
                    Section es = sectionRepository.findById(eto.sectionNo()).orElse(null);
                    e.setSection(es);
                    e.setGrade(eto.grade());
                    enrollmentRepository.save(e);
                    break;
                case "deleteEnrollment":
                    enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
                    break;
                default:
                    System.out.println("Error. unknown message " + parts[0]);
            }
        } catch (Exception e) {
            System.out.println("Exception "+e.getMessage());
        }
    }

    public void sendMessage(String cmd, Object obj) {
        String msg = cmd +" "+ asJsonString(obj);
        System.out.println("Sending "+msg);
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), msg);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}