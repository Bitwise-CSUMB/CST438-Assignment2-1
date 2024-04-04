package com.cst438.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Enrollment {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    int enrollmentId;

    // add additional attribute for grade
    private String grade;

    // create relationship between enrollment and user entities
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // create relationship between enrollment and section entities
    @ManyToOne
    @JoinColumn(name = "section_no", nullable = false)
    private Section section;

    public Enrollment() {}

    public Enrollment(int enrollmentId, String grade, User user, Section section) {
        this.enrollmentId = enrollmentId;
        this.grade = grade;
        this.user = user;
        this.section = section;
    }

    // add getter/setter methods
    public int getEnrollmentId() {
        return this.enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getGrade() {
        return this.grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Section getSection() {
        return this.section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
