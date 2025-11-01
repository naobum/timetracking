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

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepo;

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public Student addStudent(Student student){
        return studentRepo.save(student);
    }

    @Cacheable("students")
    public Optional<StudentDto> getStudentById(Long studentId) {
        return studentRepo.findById(studentId)
            .map(StudentMappings::toStudentDto);
    }

    @Transactional
    @Cacheable("students")
    public void deleteStudent(Long studentId) {
        Student deletingStudent = studentRepo.getReferenceById(studentId);
        studentRepo.delete(deletingStudent);
    }
}
