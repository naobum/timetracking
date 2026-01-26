package com.kfu.timetracking.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kfu.timetracking.models.SubjectTask;
import com.kfu.timetracking.models.TaskType;

@Repository
public interface SubjectTaskRepository extends JpaRepository<SubjectTask, Long> {
    Optional<SubjectTask> findBySubjectAndTaskType(String subject, TaskType taskType);
}
