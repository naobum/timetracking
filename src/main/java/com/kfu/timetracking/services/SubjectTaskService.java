package com.kfu.timetracking.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kfu.timetracking.models.SubjectTask;
import com.kfu.timetracking.models.TaskType;
import com.kfu.timetracking.repositories.SubjectTaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectTaskService {
    private final SubjectTaskRepository subjectTaskRepository;

    public int uploadSubjectTasksFromCsv(MultipartFile file) throws IOException {
        List<SubjectTask> subjectTasks = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            
            while ((line = reader.readLine()) != null) {           
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    log.warn("Неверный формат строки CSV: {}", line);
                    continue;
                }
                
                try {
                    TaskType taskType = TaskType.valueOf(parts[0].trim().toUpperCase());
                    String subject = parts[1].trim();
                    Double expectedHours = Double.parseDouble(parts[2].trim());
                    
                    SubjectTask task = new SubjectTask();
                    task.setTaskType(taskType);
                    task.setSubject(subject);
                    task.setExpectedHours(expectedHours);
                    
                    subjectTasks.add(task);
                } catch (IllegalArgumentException e) {
                    log.warn("Ошибка парсинга строки: {}. Ошибка: {}", line, e.getMessage());
                }
            }
        }
        
        subjectTaskRepository.saveAll(subjectTasks);
        log.info("Успешно загружено {} записей", subjectTasks.size());
        return subjectTasks.size();
    }
}
