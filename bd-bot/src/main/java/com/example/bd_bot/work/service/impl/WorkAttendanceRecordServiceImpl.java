package com.example.bd_bot.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bd_bot.work.dao.HolidayCalendarDao;
import com.example.bd_bot.work.dto.WorkAttendanceRecordImportResult;
import com.example.bd_bot.work.dto.WorkOvertimeDailyDetail;
import com.example.bd_bot.work.dto.WorkOvertimeMonthlyStatResult;
import com.example.bd_bot.work.dto.WorkOvertimeMonthlySummary;
import com.example.bd_bot.work.entity.HolidayCalendarEntity;
import com.example.bd_bot.work.entity.WorkAttendanceRecordEntity;
import com.example.bd_bot.work.service.WorkAttendanceRecordService;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WorkAttendanceRecordServiceImpl implements WorkAttendanceRecordService {

    private static final LocalTime LUNCH_START_TIME = LocalTime.of(12, 0);

    private static final LocalTime LUNCH_END_TIME = LocalTime.of(13, 30);

    /**
     * 工作日加班触发时间。
     * 最后一次打卡必须超过 19:00，才算当天有加班。
     */
    private static final LocalTime WORKDAY_OVERTIME_TRIGGER_TIME = LocalTime.of(19, 0);

    /**
     * 工作日加班计算起点。
     * 只要最后一次打卡超过 19:00，18:00 到 19:00 的 1 小时也计入加班。
     */
    private static final LocalTime WORKDAY_OVERTIME_START_TIME = LocalTime.of(18, 0);

    /**
     * 工作日加班最小计量单位：30 分钟。
     */
    private static final int WORKDAY_OVERTIME_UNIT_MINUTES = 30;

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd")
    };

    private static final DateTimeFormatter[] TIME_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm")
    };

    @Resource
    private HolidayCalendarDao holidayCalendarDao;

    /**
     * 打卡 Excel 导入。
     *
     * @param file 文件
     * @return 导入结果
     */
    @Override
    public WorkAttendanceRecordImportResult importAttendanceRecords(MultipartFile file) {
        ParseResult parseResult = parseAttendanceRecords(file);
        return new WorkAttendanceRecordImportResult(parseResult.getRecords().size(), parseResult.getRecords(), parseResult.getErrors());
    }

    /**
     * 按月统计加班时间。
     *
     * @param file 打卡 Excel 文件
     * @param name 姓名
     * @return 每人每月加班统计
     */
    @Override
    public WorkOvertimeMonthlyStatResult calculateMonthlyOvertime(MultipartFile file, String name) {
        // 1. 解析 Excel。导入的是公司全员打卡名单，这里先拿到全部有效打卡记录。
        ParseResult parseResult = parseAttendanceRecords(file);

        // 2. 按接口传入的单个姓名过滤，只返回该人员的加班记录。
        List<WorkAttendanceRecordEntity> overtimeRecords = filterOvertimeRecords(parseResult.getRecords(), name);

        // 3. 查询参与统计人员涉及年份的节假日配置。
        // holiday = true  表示法定放假，按休息日计算；
        // holiday = false 表示调休补班，按工作日计算；
        // 没有节假日配置时，默认周一到周五工作、周六周日休息。
        Map<LocalDate, HolidayCalendarEntity> holidayMap = queryHolidayMap(overtimeRecords);

        // 4. 将多次打卡归并成“某个人某一天”的打卡数据。
        // 工作日只使用最晚打卡时间判断是否超过 19:00；
        // 超过 19:00 后，加班时间从 18:00 开始算。
        // 休息日/节假日使用最早打卡到最晚打卡的时长计算全天加班。
        Map<String, DayAttendance> dayAttendanceMap = groupByNameAndDate(overtimeRecords);

        // 5. 月汇总临时容器。key = 姓名|yyyy-MM。
        Map<String, WorkOvertimeMonthlySummary> monthlySummaryMap = new HashMap<String, WorkOvertimeMonthlySummary>();

        // 6. 固定排序，保证接口返回稳定。
        List<DayAttendance> dayAttendances = new ArrayList<DayAttendance>(dayAttendanceMap.values());
        dayAttendances.sort(Comparator
                .comparing(DayAttendance::getName)
                .thenComparing(DayAttendance::getAttendanceDate));

        for (DayAttendance dayAttendance : dayAttendances) {
            // 7. 计算当天加班明细。
            WorkOvertimeDailyDetail detail = buildDailyOvertimeDetail(dayAttendance, holidayMap.get(dayAttendance.getAttendanceDate()));
            String month = YearMonth.from(dayAttendance.getAttendanceDate()).toString();
            String summaryKey = detail.getName() + "|" + month;

            // 8. 初始化该人员该月份的汇总对象。
            WorkOvertimeMonthlySummary summary = monthlySummaryMap.get(summaryKey);
            if (summary == null) {
                summary = new WorkOvertimeMonthlySummary();
                summary.setName(detail.getName());
                summary.setMonth(month);
                summary.setTotalOvertimeMinutes(0);
                summary.setTotalOvertimeHours(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                monthlySummaryMap.put(summaryKey, summary);
            }

            // 9. 累加当天明细。小时数统一由总分钟换算，避免小数累计误差。
            summary.getDetails().add(detail);
            summary.setTotalOvertimeMinutes(summary.getTotalOvertimeMinutes() + detail.getOvertimeMinutes());
            summary.setTotalOvertimeHours(toHours(summary.getTotalOvertimeMinutes()));
        }

        // 10. 汇总排序后返回。
        List<WorkOvertimeMonthlySummary> summaries = new ArrayList<WorkOvertimeMonthlySummary>(monthlySummaryMap.values());
        summaries.sort(Comparator
                .comparing(WorkOvertimeMonthlySummary::getName)
                .thenComparing(WorkOvertimeMonthlySummary::getMonth));

        WorkOvertimeMonthlyStatResult result = new WorkOvertimeMonthlyStatResult();
        result.setCode(0);
        result.setSuccessCount(overtimeRecords.size());
        result.setErrors(parseResult.getErrors());
        result.setSummaries(summaries);
        return result;
    }

    private List<WorkAttendanceRecordEntity> filterOvertimeRecords(List<WorkAttendanceRecordEntity> records, String name) {
        String overtimeName = getOvertimeName(name);

        List<WorkAttendanceRecordEntity> filteredRecords = new ArrayList<WorkAttendanceRecordEntity>();
        for (WorkAttendanceRecordEntity record : records) {
            if (overtimeName.equals(record.getName())) {
                filteredRecords.add(record);
            }
        }
        if (filteredRecords.isEmpty()) {
            throw new IllegalArgumentException("员工不存在：" + overtimeName);
        }
        return filteredRecords;
    }

    private String getOvertimeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        String trimmedName = name.trim();
        if (trimmedName.contains(",") || trimmedName.contains("，")) {
            throw new IllegalArgumentException("每次仅允许传入一个姓名");
        }
        return trimmedName;
    }

    private ParseResult parseAttendanceRecords(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        List<WorkAttendanceRecordEntity> records = new ArrayList<WorkAttendanceRecordEntity>();
        List<String> errors = new ArrayList<String>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            validateHeader(sheet, formatter);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlankRow(row, formatter)) {
                    continue;
                }

                try {
                    records.add(parseRecord(row, formatter));
                } catch (RuntimeException ex) {
                    errors.add("第 " + (rowIndex + 1) + " 行：" + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Excel 文件读取失败", ex);
        }

        ParseResult result = new ParseResult();
        result.setRecords(records);
        result.setErrors(errors);
        return result;
    }

    private WorkAttendanceRecordEntity parseRecord(Row row, DataFormatter formatter) {
        String name = formatter.formatCellValue(row.getCell(0)).trim();
        LocalDate attendanceDate = parseDate(row.getCell(1), formatter);
        LocalTime attendanceTime = parseTime(row.getCell(2), formatter);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        WorkAttendanceRecordEntity record = new WorkAttendanceRecordEntity();
        record.setName(name);
        record.setAttendanceDate(attendanceDate);
        record.setAttendanceTime(attendanceTime);
        return record;
    }

    private void validateHeader(Sheet sheet, DataFormatter formatter) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("Excel 第一行必须是表头：姓名、日期、时间");
        }

        List<String> expectedHeaders = Arrays.asList("姓名", "日期", "时间");
        for (int i = 0; i < expectedHeaders.size(); i++) {
            String actual = formatter.formatCellValue(header.getCell(i)).trim();
            if (!expectedHeaders.get(i).equals(actual)) {
                throw new IllegalArgumentException("Excel 表头格式错误，第一行必须是：姓名、日期、时间");
            }
        }
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i < 3; i++) {
            if (!formatter.formatCellValue(row.getCell(i)).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDate(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        if (DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("日期不能为空");
        }

        for (DateTimeFormatter dateFormatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, dateFormatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("日期格式错误，应为 yyyy/M/d，例如 2026/3/2");
    }

    private LocalTime parseTime(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            throw new IllegalArgumentException("时间不能为空");
        }

        if (DateUtil.isCellDateFormatted(cell) || cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            double timeFraction = numericValue - Math.floor(numericValue);
            int totalSeconds = (int) Math.round(timeFraction * 24 * 60 * 60);
            if (totalSeconds >= 24 * 60 * 60) {
                totalSeconds = 0;
            }
            return LocalTime.ofSecondOfDay(totalSeconds);
        }

        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("时间不能为空");
        }

        for (DateTimeFormatter timeFormatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(value, timeFormatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("时间格式错误，应为 H:mm:ss，例如 8:13:56");
    }

    private Map<LocalDate, HolidayCalendarEntity> queryHolidayMap(List<WorkAttendanceRecordEntity> records) {
        Set<Integer> years = new HashSet<Integer>();
        for (WorkAttendanceRecordEntity record : records) {
            years.add(record.getAttendanceDate().getYear());
        }

        Map<LocalDate, HolidayCalendarEntity> holidayMap = new HashMap<LocalDate, HolidayCalendarEntity>();
        if (years.isEmpty()) {
            return holidayMap;
        }

        List<HolidayCalendarEntity> holidays = holidayCalendarDao.selectList(new LambdaQueryWrapper<HolidayCalendarEntity>()
                .in(HolidayCalendarEntity::getYear, years));
        for (HolidayCalendarEntity holiday : holidays) {
            holidayMap.put(holiday.getHolidayDate(), holiday);
        }
        return holidayMap;
    }

    private Map<String, DayAttendance> groupByNameAndDate(List<WorkAttendanceRecordEntity> records) {
        Map<String, DayAttendance> dayAttendanceMap = new HashMap<String, DayAttendance>();
        for (WorkAttendanceRecordEntity record : records) {
            String key = record.getName() + "|" + record.getAttendanceDate();
            DayAttendance dayAttendance = dayAttendanceMap.get(key);
            if (dayAttendance == null) {
                dayAttendance = new DayAttendance();
                dayAttendance.setName(record.getName());
                dayAttendance.setAttendanceDate(record.getAttendanceDate());
                dayAttendance.setFirstPunchTime(record.getAttendanceTime());
                dayAttendance.setLastPunchTime(record.getAttendanceTime());
                dayAttendanceMap.put(key, dayAttendance);
                continue;
            }

            if (record.getAttendanceTime().isBefore(dayAttendance.getFirstPunchTime())) {
                dayAttendance.setFirstPunchTime(record.getAttendanceTime());
            }
            if (record.getAttendanceTime().isAfter(dayAttendance.getLastPunchTime())) {
                dayAttendance.setLastPunchTime(record.getAttendanceTime());
            }
        }
        return dayAttendanceMap;
    }

    private WorkOvertimeDailyDetail buildDailyOvertimeDetail(DayAttendance dayAttendance, HolidayCalendarEntity holiday) {
        LocalDate attendanceDate = dayAttendance.getAttendanceDate();
        boolean workday = isWorkday(attendanceDate, holiday);
        int overtimeMinutes = workday
                ? calculateWorkdayOvertimeMinutes(dayAttendance.getLastPunchTime())
                : calculateRestDayOvertimeMinutes(dayAttendance.getFirstPunchTime(), dayAttendance.getLastPunchTime());

        WorkOvertimeDailyDetail detail = new WorkOvertimeDailyDetail();
        detail.setName(dayAttendance.getName());
        detail.setAttendanceDate(attendanceDate);
        detail.setFirstPunchTime(dayAttendance.getFirstPunchTime());
        detail.setLastPunchTime(dayAttendance.getLastPunchTime());
        detail.setWorkday(workday);
        detail.setHoliday(holiday == null ? null : holiday.getHoliday());
        detail.setHolidayName(holiday == null ? null : holiday.getName());
        detail.setOvertimeMinutes(overtimeMinutes);
        detail.setOvertimeHours(toHours(overtimeMinutes));
        return detail;
    }

    private boolean isWorkday(LocalDate date, HolidayCalendarEntity holiday) {
        if (holiday != null && holiday.getHoliday() != null) {
            return !holiday.getHoliday();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private int calculateWorkdayOvertimeMinutes(LocalTime lastPunchTime) {
        if (!lastPunchTime.isAfter(WORKDAY_OVERTIME_TRIGGER_TIME)) {
            return 0;
        }

        int overtimeMinutes = minutesBetween(WORKDAY_OVERTIME_START_TIME, lastPunchTime);
        return overtimeMinutes / WORKDAY_OVERTIME_UNIT_MINUTES * WORKDAY_OVERTIME_UNIT_MINUTES;
    }

    private int calculateRestDayOvertimeMinutes(LocalTime firstPunchTime, LocalTime lastPunchTime) {
        if (!lastPunchTime.isAfter(firstPunchTime)) {
            return 0;
        }

        int totalMinutes = minutesBetween(firstPunchTime, lastPunchTime);
        int lunchMinutes = overlapMinutes(firstPunchTime, lastPunchTime, LUNCH_START_TIME, LUNCH_END_TIME);
        return Math.max(totalMinutes - lunchMinutes, 0);
    }

    private int overlapMinutes(LocalTime startTime, LocalTime endTime, LocalTime overlapStartTime, LocalTime overlapEndTime) {
        LocalTime start = max(startTime, overlapStartTime);
        LocalTime end = min(endTime, overlapEndTime);
        if (!end.isAfter(start)) {
            return 0;
        }
        return minutesBetween(start, end);
    }

    private int minutesBetween(LocalTime startTime, LocalTime endTime) {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    private BigDecimal toHours(Integer minutes) {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private LocalTime min(LocalTime firstTime, LocalTime secondTime) {
        return firstTime.isBefore(secondTime) ? firstTime : secondTime;
    }

    private LocalTime max(LocalTime firstTime, LocalTime secondTime) {
        return firstTime.isAfter(secondTime) ? firstTime : secondTime;
    }

    @Data
    private static class ParseResult {
        private List<WorkAttendanceRecordEntity> records;
        private List<String> errors;
    }

    @Data
    private static class DayAttendance {
        private String name;
        private LocalDate attendanceDate;
        private LocalTime firstPunchTime;
        private LocalTime lastPunchTime;
    }
}
