package com.example.demo.service;

import com.example.demo.vo.ExamPaperVO;
import com.example.demo.vo.ExamResultVO;
import com.example.demo.vo.SubmitExamDTO;

public interface ExamService {
    Long createExam(String title, String startTime, String endTime, int questionCount);
    ExamPaperVO getExamPaper(Long examId);

    // 新增：自动判卷并记录数据
    ExamResultVO submitExam(SubmitExamDTO submitDTO);
}