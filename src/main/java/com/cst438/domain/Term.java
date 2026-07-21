package com.cst438.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Date;

@Entity
public class Term {
    @Id
    private int termId;
    @Column(name="tyear")
    private int year;
    private String semester;
    private Date addDate;
    private Date addDeadline;
    private Date dropDeadline;
    private Date startDate;
    private Date endDate;

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public Date getAddDeadline() {
        return addDeadline;
    }

    public void setAddDeadline(Date addDeadline) {
        this.addDeadline = addDeadline;
    }

    public Date getDropDeadline() {
        return dropDeadline;
    }

    public void setDropDeadline(Date dropDeadline) {
        this.dropDeadline = dropDeadline;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
