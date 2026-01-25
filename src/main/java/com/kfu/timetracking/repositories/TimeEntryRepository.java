package com.kfu.timetracking.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.models.TimeEntry;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByStudent(Student student);

    Optional<TimeEntry> findByStudentAndEndIsNull(Student student);

    List<TimeEntry> findByStudentAndStartBetween(Student student, LocalDateTime weekStart, LocalDateTime weekEnd);

    List<TimeEntry> findByDescriptionContainingIgnoreCase(String subject);
}
