package com.example.demo.vo;

import lombok.Data;
import java.util.List;

@Data
public class SubmitExamDTO {
    private Long examId;         // 考试ID
    private String studentId;    // 学号
    private String studentName;  // 姓名
    private List<AnswerItem> answers; // 答题明细

    @Data
    public static class AnswerItem {
        private Long wordId;        // 单词ID
        private String selectedAnswer; // 学生选择的中文释义
    }
}