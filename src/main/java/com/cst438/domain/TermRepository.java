package com.cst438.domain;

import org.springframework.data.repository.CrudRepository;

public interface TermRepository extends CrudRepository<Term, Integer> {

    Term findByYearAndSemester( int year, String semester);

//    List<Term> findAllByOrderByTermIdDesc();
}
