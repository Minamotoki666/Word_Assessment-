package com.example.demo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamPaperVO {
    private Long examId;              // 考试ID
    private String title;             // 考试标题
    private LocalDateTime startTime;  // 开始时间
    private LocalDateTime endTime;    // 结束时间
    private List<QuestionVO> questions; // 打乱后的题目列表
}