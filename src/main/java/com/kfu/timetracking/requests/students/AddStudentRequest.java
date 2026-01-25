package com.kfu.timetracking.requests.students;

import lombok.Data;

@Data
public class AddStudentRequest {
    public String name;
    public String groupName;
}
