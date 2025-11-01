package com.kfu.timetracking.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.requests.students.AddStudentRequest;
import com.kfu.timetracking.responses.students.StudentDto;
import com.kfu.timetracking.services.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestPart;



@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Управление студентами")
@RequiredArgsConstructor
public class StudentsController {
    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Добавление студента в БД", description = "Возвращает информацию о добавленном студенте")
    public ResponseEntity<Student> addStudent(@RequestBody AddStudentRequest param) {
        Student addingStudent = new Student();
        addingStudent.setGroupName(param.getGroupName());
        addingStudent.setName(param.getName());

        studentService.addStudent(addingStudent);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(addingStudent);
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Получить студента по ID", description = "Возвращает информацию о студенте")
    public ResponseEntity<StudentDto> getStudent(@PathVariable Long studentId) {
        var student = studentService.getStudentById(studentId);

        if (student.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(student.get());
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "Удалить студента из БД по ID", description = "Возвращает NoContent")
        public ResponseEntity<Void> deleteStudent(@RequestPart Long studentId) {
        studentService.deleteStudent(studentId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
