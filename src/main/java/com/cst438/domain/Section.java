package com.cst438.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Section {

    @Id
    private int sectionNo;  // unique id assigned by database.
    private int sectionId;
    @ManyToOne
    @JoinColumn(name="course_id", nullable=false)
    private Course course;
    private String building;
    private String room;
    private String times;
    private String instructorEmail;
    @ManyToOne
    @JoinColumn(name="term_id", nullable=false)
    private Term term;

    @OneToMany(mappedBy="section")
    List<Enrollment> enrollments;

    @OneToMany(mappedBy="section")
    @OrderBy("dueDate")
    List<Assignment> assignments;

    public int getSectionNo() {
        return sectionNo;
    }

    public void setSectionNo(int sectionNo) {
        this.sectionNo = sectionNo;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getInstructorEmail() {
        return instructorEmail;
    }

    public void setInstructorEmail(String instructorEmail) {
        this.instructorEmail = instructorEmail;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}
