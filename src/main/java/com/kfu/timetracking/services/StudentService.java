package com.kfu.timetracking.services;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.repositories.StudentRepository;
import com.kfu.timetracking.responses.students.StudentDto;
import com.kfu.timetracking.responses.students.StudentMappings;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    private final StudentRepository studentRepo;

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public Student addStudent(Student student){
        log.info("Добавление нового студента");
        Student savedStudent = studentRepo.save(student);
        log.info("Студент успешно добавлен с ID: {}", savedStudent.getId());
        return savedStudent;
    }

    @Cacheable("students")
    public Optional<StudentDto> getStudentById(Long studentId) {
        log.debug("Получение студента с ID: {}", studentId);
        return studentRepo.findById(studentId)
            .map(student -> {
                log.info("Студент найден: ID {}", studentId);
                return StudentMappings.toStudentDto(student);
            });
    }

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public void deleteStudent(Long studentId) {
        log.info("Удаление студента с ID: {}", studentId);
        Student deletingStudent = studentRepo.getReferenceById(studentId);
        studentRepo.delete(deletingStudent);
        log.info("Студент с ID: {} успешно удален", studentId);
    }
}
