package com.example.demo.service;

import com.example.demo.vo.ExamPaperVO;

public interface ExamService {
    Long createExam(String title, String startTime, String endTime, int questionCount);

    ExamPaperVO getExamPaper(Long examId);
}