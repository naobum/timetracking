package com.kfu.timetracking.responses.students;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentDto {
    private Long id;
    private String name;
    private String groupName;
}
