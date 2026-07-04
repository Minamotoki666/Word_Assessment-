package com.example.demo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ColumnWidth(20) // 设置全局列宽
public class ExamRecordExportVO {

    @ExcelProperty("记录编号")
    private Long id;

    @ExcelProperty("考试ID")
    private Long examId;

    @ExcelProperty("学号")
    private String studentId;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("考试得分(分)")
    private Integer score;

    @ExcelProperty("交卷时间")
    private LocalDateTime submitTime;
}