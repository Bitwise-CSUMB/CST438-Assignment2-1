package com.cst438.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    int enrollmentId;

    // TODO complete this class

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

    // One to Many relationship between enrollment and grade
    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.REMOVE)
    List<Grade> grades;

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
