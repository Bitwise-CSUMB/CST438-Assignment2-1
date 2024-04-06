package com.cst438.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.sql.Date;
import java.util.List;

@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private int assignmentId;

    // add additional attributes for title, dueDate
    private String title;

    @Column(name = "due_date")
    private Date dueDate;

    // add relationship between assignment and section entities
    @ManyToOne
    @JoinColumn(name = "section_no", nullable = false)
    private Section section;

    // One to many relationship to grade
    @OneToMany(mappedBy = "assignment")
    List<Grade> grades;

    public Assignment() {}

    public Assignment(int assignmentId, String title, Date dueDate, Section section) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.dueDate = dueDate;
        this.section = section;
    }

    // add getter and setter methods
    public int getAssignmentId() {
        return this.assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Section getSection() {
        return this.section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<Grade> getGrades() {
        return this.grades;
    }
}
