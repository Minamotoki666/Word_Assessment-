package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.example.demo.service.ExamService;
import com.example.demo.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin // 开启跨域，让以后 Vue 前端可以直接调用接口不报错
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * 1. 发起一次新考试
     * 示例：POST http://localhost:8080/api/exam/create?title=专业英语期中测试&startTime=2026-07-04 08:00:00&endTime=2026-07-04 10:00:00&questionCount=10
     */
    @PostMapping("/create")
    public Long createExam(@RequestParam String title,
                           @RequestParam String startTime,
                           @RequestParam String endTime,
                           @RequestParam int questionCount) {
        return examService.createExam(title, startTime, endTime, questionCount);
    }

    /**
     * 2. 考生动态拉取试卷（自动按考生打乱题序与选项顺序）
     * 示例：GET http://localhost:8080/api/exam/paper?examId=1
     */
    @GetMapping("/paper")
    public ExamPaperVO getExamPaper(@RequestParam Long examId) {
        return examService.getExamPaper(examId);
    }

    /**
     * 3. 考生交卷并自动判分、记录答题明细
     * 示例：POST http://localhost:8080/api/exam/submit
     */
    @PostMapping("/submit")
    public ExamResultVO submitExam(@RequestBody SubmitExamDTO submitDTO) {
        return examService.submitExam(submitDTO);
    }

    /**
     * 4. 获取指定学生的每周答错词汇字符云数据
     * 示例：GET http://localhost:8080/api/exam/wordcloud?studentId=20240001
     */
    @GetMapping("/wordcloud")
    public List<WordCloudVO> getWordCloud(@RequestParam String studentId) {
        return examService.getWeeklyWordCloud(studentId);
    }

    /**
     * 5. 一键导出考试成绩为 Excel 表格
     * 在浏览器直接访问即可下载：GET http://localhost:8080/api/exam/export?examId=1
     */
    @GetMapping("/export")
    public void exportExamRecords(HttpServletResponse response,
                                  @RequestParam(required = false) Long examId) throws IOException {
        // 设置响应头，告诉浏览器下载 Excel 文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("英文考试统计报表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 查询要导出的列表数据
        List<ExamRecordExportVO> dataList = examService.getExportRecordList(examId);

        // 调用阿里 EasyExcel 输出流
        EasyExcel.write(response.getOutputStream(), ExamRecordExportVO.class)
                .sheet("成绩统计表")
                .doWrite(dataList);
    }
}