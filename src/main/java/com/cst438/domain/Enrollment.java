package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Enrollment {
    @Id
    int enrollmentId;
    String grade;

    @ManyToOne
    @JoinColumn(name="section_no", nullable=false)
    private Section section;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User student;

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }
}
