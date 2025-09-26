package com.kfu.timetracking.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kfu.timetracking.models.TimeEntry;

@Repository
public interface TimeTrackingRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByStudentId(Long studentId);

    Optional<TimeEntry> findByStudentIdAndEndIsNull(Long studentId);
}
