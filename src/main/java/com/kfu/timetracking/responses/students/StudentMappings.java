package com.kfu.timetracking.responses.students;

import com.kfu.timetracking.models.Student;

public class StudentMappings {
    public static StudentDto toStudentDto(Student student) {
        return new StudentDto(student.getId(),  student.getName(), student.getGroupName());
    }
}
