package com.example.demo.vo;

import lombok.Data;

@Data
public class ExamResultVO {
    private Long recordId;   // 本次考试记录ID
    private Integer score;   // 最终得分
    private Integer total;   // 总分(总题数)
}