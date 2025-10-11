package com.kfu.timetracking.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.repositories.StudentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepo;

    @Transactional
    public Student addStudent(Student student){
        return studentRepo.save(student);
    }

    public Optional<Student> getStudentById(Long studentId) {
        return studentRepo.findById(studentId);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student deletingStudent = studentRepo.getReferenceById(studentId);
        studentRepo.delete(deletingStudent);
    }
}
