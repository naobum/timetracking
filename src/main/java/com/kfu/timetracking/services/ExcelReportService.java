package com.kfu.timetracking.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.kfu.timetracking.models.Student;
import com.kfu.timetracking.models.TimeEntry;
import com.kfu.timetracking.repositories.StudentRepository;
import com.kfu.timetracking.repositories.TimeEntryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для генерации Excel отчётов о затреканом времени с графиками
 */
@Service
@Slf4j
public class ExcelReportService {
    private final StudentRepository studentRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Autowired
    public ExcelReportService(StudentRepository studentRepository, TimeEntryRepository timeEntryRepository) {
        this.studentRepository = studentRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    /**
     * Генерирует Excel отчёт с статистикой времени за неделю
     * @param studentId ID студента
     * @return массив байтов с содержимым Excel файла
     */
    public byte[] generateWeeklyReport(Long studentId) throws IOException {
        log.info("Генерация недельного отчёта для студента с ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + studentId + " не найден"));

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime weekStart = today.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<TimeEntry> timeEntries = timeEntryRepository.findByStudentAndStartBetween(student, weekStart, today);
        log.info("generateWeeklyReport: найдено записей в БД: {}", timeEntries.size());
        for (TimeEntry entry : timeEntries) {
            log.debug("Запись: start={}, end={}", entry.getStart(), entry.getEnd());
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet reportSheet = workbook.createSheet("Недельный отчёт");
            
            // Создание и заполнение заголовков и данных
            createReportHeader(reportSheet, student);
            createReportData(reportSheet, timeEntries);
            
            // Создание листа с данными для графика
            XSSFSheet chartDataSheet = workbook.createSheet("Данные для графика");
            createChartData(chartDataSheet, timeEntries);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            log.info("Недельный отчёт для студента с ID: {} успешно сгенерирован", studentId);
            return outputStream.toByteArray();
        } finally {
            workbook.close();
        }
    }

    /**
     * Генерирует Excel отчёт с статистикой времени за месяц
     * @param studentId ID студента
     * @return массив байтов с содержимым Excel файла
     */
    public byte[] generateMonthlyReport(Long studentId) throws IOException {
        log.info("Генерация месячного отчёта для студента с ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Студент с ID " + studentId + " не найден"));

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<TimeEntry> timeEntries = timeEntryRepository.findByStudentAndStartBetween(student, monthStart, today);

        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet reportSheet = workbook.createSheet("Месячный отчёт");
            
            createReportHeader(reportSheet, student);
            createReportData(reportSheet, timeEntries);
            
            // Создание листа с данными для графика
            XSSFSheet chartDataSheet = workbook.createSheet("Данные для графика");
            createChartData(chartDataSheet, timeEntries);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            log.info("Месячный отчёт для студента с ID: {} успешно сгенерирован", studentId);
            return outputStream.toByteArray();
        } finally {
            workbook.close();
        }
    }

    /**
     * Создает заголовок отчёта
     */
    private void createReportHeader(XSSFSheet sheet, Student student) {
        XSSFCellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        XSSFFont headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeight(14);
        headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0, (byte) 102, (byte) 204}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Заголовок
        XSSFRow headerRow = sheet.createRow(0);
        XSSFCell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Отчёт о затреканом времени");
        headerCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // Информация о студенте
        XSSFRow infoRow = sheet.createRow(2);
        infoRow.createCell(0).setCellValue("Студент:");
        infoRow.createCell(1).setCellValue(student.getName());

        infoRow = sheet.createRow(3);
        infoRow.createCell(0).setCellValue("Группа:");
        infoRow.createCell(1).setCellValue(student.getGroupName());

        infoRow = sheet.createRow(4);
        infoRow.createCell(0).setCellValue("Дата отчёта:");
        infoRow.createCell(1).setCellValue(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    /**
     * Заполняет таблицу данными о затреканом времени
     */
    private void createReportData(XSSFSheet sheet, List<TimeEntry> timeEntries) {
        XSSFCellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        XSSFFont headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 79, (byte) 129, (byte) 189}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        XSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        // Заголовки таблицы
        XSSFRow headerRow = sheet.createRow(6);
        String[] headers = {"Дата", "Начало", "Конец", "Продолжительность (часы)", "Тип", "Описание"};
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Данные
        int rowNum = 7;
        for (TimeEntry entry : timeEntries) {
            if (entry.getEnd() != null) {
                XSSFRow row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(entry.getStart().toLocalDate().toString());
                row.createCell(1).setCellValue(entry.getStart().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                row.createCell(2).setCellValue(entry.getEnd().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                
                long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(entry.getStart(), entry.getEnd());
                double durationHours = durationMinutes / 60.0;
                row.createCell(3).setCellValue(String.format("%.2f", durationHours));
                
                row.createCell(4).setCellValue(entry.getType() != null ? entry.getType().toString() : "");
                row.createCell(5).setCellValue(entry.getDescription() != null ? entry.getDescription() : "");

                for (int i = 0; i < 6; i++) {
                    row.getCell(i).setCellStyle(cellStyle);
                }
            }
        }

        // Автоматическая ширина столбцов
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        // Итого время
        XSSFRow totalRow = sheet.createRow(rowNum + 1);
        totalRow.createCell(0).setCellValue("ИТОГО:");
        
        double totalHours = 0;
        for (TimeEntry entry : timeEntries) {
            if (entry.getEnd() != null) {
                long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(entry.getStart(), entry.getEnd());
                totalHours += durationMinutes / 60.0;
            }
        }
        totalRow.createCell(3).setCellValue(String.format("%.2f часов", totalHours));

        XSSFCellStyle totalStyle = sheet.getWorkbook().createCellStyle();
        XSSFFont totalFont = sheet.getWorkbook().createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalFont);
        totalStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 217, (byte) 217, (byte) 217}, null));
        totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < 6; i++) {
            if (totalRow.getCell(i) != null) {
                totalRow.getCell(i).setCellStyle(totalStyle);
            }
        }
    }

    /**
     * Создает таблицу данных для построения графика по дням
     */
    private void createChartData(XSSFSheet sheet, List<TimeEntry> timeEntries) {
        // Подготовка данных для графика - суммирование по датам
        log.info("createChartData: входящих записей: {}", timeEntries.size());
        
        Map<LocalDate, Double> dailyHours = new TreeMap<>();
        for (TimeEntry entry : timeEntries) {
            log.debug("Запись: start={}, end={}, end is null: {}", entry.getStart(), entry.getEnd(), entry.getEnd() == null);
            if (entry.getEnd() != null) {
                LocalDate date = entry.getStart().toLocalDate();
                long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(entry.getStart(), entry.getEnd());
                double durationHours = durationMinutes / 60.0;
                dailyHours.put(date, dailyHours.getOrDefault(date, 0.0) + durationHours);
                log.debug("Добавлена запись за {}:  {} часов", date, durationHours);
            }
        }

        XSSFCellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        XSSFFont headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0, (byte) 102, (byte) 204}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        // Заголовки
        XSSFRow titleRow = sheet.createRow(0);
        XSSFCell dateHeader = titleRow.createCell(0);
        dateHeader.setCellValue("Дата");
        dateHeader.setCellStyle(headerStyle);
        
        XSSFCell hoursHeader = titleRow.createCell(1);
        hoursHeader.setCellValue("Часы");
        hoursHeader.setCellStyle(headerStyle);

        // Данные для графика
        int rowNum = 1;
        for (Map.Entry<LocalDate, Double> entry : dailyHours.entrySet()) {
            XSSFRow row = sheet.createRow(rowNum++);
            
            XSSFCell dateCell = row.createCell(0);
            dateCell.setCellValue(entry.getKey().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            dateCell.setCellStyle(cellStyle);
            
            XSSFCell hoursCell = row.createCell(1);
            hoursCell.setCellValue(entry.getValue());
            hoursCell.setCellStyle(cellStyle);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        log.info("Таблица данных для графика создана с {} записями", dailyHours.size());
    }
}
