package com.kfu.timetracking.services;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.repositories.StudentRepository;
import com.kfu.timetracking.responses.students.StudentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;
    private StudentDto testStudentDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setName("John Doe");
        testStudent.setGroupName("ИВТ-101");

        testStudentDto = new StudentDto(1L, "John Doe", "ИВТ-101");
    }

    @Test
    void testAddStudentSuccess() {
        when(studentRepository.save(testStudent)).thenReturn(testStudent);

        Student result = studentService.addStudent(testStudent);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("ИВТ-101", result.getGroupName());
        verify(studentRepository, times(1)).save(testStudent);
    }

    @Test
    void testGetStudentByIdSuccess() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        Optional<StudentDto> result = studentService.getStudentById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetStudentByIdNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<StudentDto> result = studentService.getStudentById(999L);

        assertTrue(result.isEmpty());
        verify(studentRepository, times(1)).findById(999L);
    }

    @Test
    void testDeleteStudentSuccess() {
        when(studentRepository.getReferenceById(1L)).thenReturn(testStudent);

        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).getReferenceById(1L);
        verify(studentRepository, times(1)).delete(testStudent);
    }

    @Test
    void testAddStudentWithNullName() {
        Student student = new Student();
        student.setName(null);

        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.addStudent(student);

        assertNotNull(result);
        assertNull(result.getName());
    }

    @Test
    void testAddStudentWithEmptyEmail() {

        when(studentRepository.save(testStudent)).thenReturn(testStudent);

        Student result = studentService.addStudent(testStudent);

        assertNotNull(result);
    }
}
