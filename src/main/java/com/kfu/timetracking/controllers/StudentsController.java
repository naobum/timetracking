package com.kfu.timetracking.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestPart;

/**
 * REST контроллер для управления студентами.
 * Предоставляет операции для добавления, получения и удаления студентов.
 */
@RestController
@RequestMapping("/api/students")
@Tag(
    name = "Students",
    description = "Управление студентами. Позволяет добавлять, получать и удалять информацию о студентах. " +
                  "Все операции требуют аутентификации и соответствующих прав доступа."
)
@RequiredArgsConstructor
public class StudentsController {
    private final StudentService studentService;

    /**
     * Добавляет нового студента в базу данных.
     * Требует прав доступа STUDENTS:WRITE.
     * 
     * @param param данные студента (имя, группа)
     * @return созданный студент с HTTP статусом 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('STUDENTS:WRITE')")
    @Operation(
        summary = "Добавление нового студента",
        description = "Создает запись о новом студенте в системе. " +
                      "Требует прав доступа STUDENTS:WRITE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Студент успешно добавлен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Student.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные студента"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Ошибка валидации входных данных"
        )
    })
    public ResponseEntity<Student> addStudent(@RequestBody AddStudentRequest param) {
        Student addingStudent = new Student();
        addingStudent.setGroupName(param.getGroupName());
        addingStudent.setName(param.getName());

        studentService.addStudent(addingStudent);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(addingStudent);
    }

    /**
     * Получает информацию о студенте по его ID.
     * Требует прав доступа STUDENTS:READ.
     * 
     * @param studentId уникальный идентификатор студента
     * @return информация о студенте в виде DTO с HTTP статусом 200 (OK)
     *         или пустой ответ с HTTP статусом 404 (Not Found) если студент не найден
     */
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('STUDENTS:READ')")
    @Operation(
        summary = "Получить студента по ID",
        description = "Возвращает информацию о студенте с указанным идентификатором. " +
                      "Требует прав доступа STUDENTS:READ."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Студент найден и возвращена его информация",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StudentDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Студент с указанным ID не найден"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован"
        )
    })
    public ResponseEntity<StudentDto> getStudent(@PathVariable Long studentId) {
        var student = studentService.getStudentById(studentId);

        if (student.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(student.get());
    }

    /**
     * Удаляет студента из базы данных по его ID.
     * Требует прав доступа STUDENTS:WRITE.
     * 
     * @param studentId уникальный идентификатор студента для удаления
     * @return пустой ответ с HTTP статусом 204 (No Content)
     */
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAuthority('STUDENTS:WRITE')")
    @Operation(
        summary = "Удалить студента",
        description = "Удаляет запись о студенте из системы по его идентификатору. " +
                      "Требует прав доступа STUDENTS:WRITE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Студент успешно удален"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав доступа"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Студент с указанным ID не найден"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован"
        )
    })
    public ResponseEntity<Void> deleteStudent(@RequestPart Long studentId) {
        studentService.deleteStudent(studentId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
