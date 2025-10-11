package com.kfu.timetracking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kfu.timetracking.models.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

}