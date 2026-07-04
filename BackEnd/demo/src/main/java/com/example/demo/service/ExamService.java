package com.example.demo.service;

import com.example.demo.vo.ExamPaperVO;
import com.example.demo.vo.ExamResultVO;
import com.example.demo.vo.SubmitExamDTO;
import com.example.demo.vo.WordCloudVO;
import com.example.demo.vo.ExamRecordExportVO;
import java.util.List;

public interface ExamService {
    Long createExam(String title, String startTime, String endTime, int questionCount);
    ExamPaperVO getExamPaper(Long examId);
    ExamResultVO submitExam(SubmitExamDTO submitDTO);

    //检查这两行，返回值一定要是 List<WordCloudVO> 和 List<ExamRecordExportVO>
    List<WordCloudVO> getWeeklyWordCloud(String studentId);
    List<ExamRecordExportVO> getExportRecordList(Long examId);
}