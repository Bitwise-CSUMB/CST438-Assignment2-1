package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Grade {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="grade_id")
    private int gradeId;
 
    // TODO complete this class

    // add additional attribute for score
    @Column(name="score")
    private int score;

    // add relationship between grade and assignment entities
    @ManyToOne
    @JoinColumn(name="assignment_id", nullable=false)
    private Assignment assignments;

    // add getter/setter methods
    public int getGradeId() {
        return gradeId;
    }
    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public Assignment getAssignment() {
        return assignments;
    }
    public void setAssignment(Assignment assignments) {
        this.assignments = assignments;
    }

}
