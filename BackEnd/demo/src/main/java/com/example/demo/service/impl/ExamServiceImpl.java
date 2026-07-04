package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.AnswerDetail;
import com.example.demo.entity.ExamInfo;
import com.example.demo.entity.ExamRecord;
import com.example.demo.entity.WordDict;
import com.example.demo.mapper.AnswerDetailMapper;
import com.example.demo.mapper.ExamInfoMapper;
import com.example.demo.mapper.ExamRecordMapper;
import com.example.demo.mapper.WordDictMapper;
import com.example.demo.service.ExamService;
import com.example.demo.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamInfoMapper examInfoMapper;

    @Autowired
    private WordDictMapper wordDictMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private AnswerDetailMapper answerDetailMapper;


    @Override
    public List<WordCloudVO> getWeeklyWordCloud(String studentId) {
        return answerDetailMapper.selectWeeklyErrorWordCloud(studentId);
    }

    @Override
    public List<ExamRecordExportVO> getExportRecordList(Long examId) {
        // 构建条件：查询特定考场的所有成绩...
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            wrapper.eq(ExamRecord::getExamId, examId);
        }
        wrapper.orderByDesc(ExamRecord::getSubmitTime);

        List<ExamRecord> records = examRecordMapper.selectList(wrapper);

        return records.stream().map(record -> {
            ExamRecordExportVO vo = new ExamRecordExportVO();
            vo.setId(record.getId());
            vo.setExamId(record.getExamId());
            vo.setStudentId(record.getStudentId());
            vo.setStudentName(record.getStudentName());
            vo.setScore(record.getScore());
            vo.setSubmitTime(record.getSubmitTime());
            return vo;
        }).collect(Collectors.toList());
    }
    @Override
    public ExamResultVO submitExam(SubmitExamDTO submitDTO) {
        // 1. 校验考试时间是否超出
        ExamInfo examInfo = examInfoMapper.selectById(submitDTO.getExamId());
        if (examInfo == null) {
            throw new RuntimeException("考试不存在！");
        }
        if (LocalDateTime.now().isAfter(examInfo.getEndTime().plusMinutes(2))) {
            // 给网络延迟预留2分钟宽限期
            throw new RuntimeException("考试已经超时结束，无法交卷！");
        }

        // 2. 批量查出本次试卷所有单词的正确答案
        List<Long> wordIds = submitDTO.getAnswers().stream()
                .map(SubmitExamDTO.AnswerItem::getWordId)
                .collect(Collectors.toList());
        List<WordDict> wordDicts = wordDictMapper.selectBatchIds(wordIds);

        // 转换成 Map<wordId, WordDict> 方便快速对答案
        Map<Long, WordDict> dictMap = wordDicts.stream()
                .collect(Collectors.toMap(WordDict::getId, w -> w));

        // 3. 先保存考生成绩主记录（为了先拿到数据库自增ID，我们暂时填0分，稍后更新）
        ExamRecord record = new ExamRecord();
        record.setExamId(submitDTO.getExamId());
        record.setStudentId(submitDTO.getStudentId());
        record.setStudentName(submitDTO.getStudentName());
        record.setScore(0);
        record.setSubmitTime(LocalDateTime.now());
        examRecordMapper.insert(record);

        // 4. 自动逐题判卷，并写入字符云关键的 answer_detail 表
        int correctCount = 0;
        for (SubmitExamDTO.AnswerItem item : submitDTO.getAnswers()) {
            WordDict correctWord = dictMap.get(item.getWordId());
            boolean isCorrect = false;

            if (correctWord != null && correctWord.getMeaningCn().equals(item.getSelectedAnswer())) {
                isCorrect = true;
                correctCount++;
            }

            // 往明细表插一条数据 (统计用)
            AnswerDetail detail = new AnswerDetail();
            detail.setExamRecordId(record.getId());
            detail.setStudentId(submitDTO.getStudentId());
            detail.setWordId(item.getWordId());
            if (correctWord != null) {
                detail.setWordEn(correctWord.getWordEn());
            } else {
                detail.setWordEn("unknown");
            }
            detail.setIsCorrect(isCorrect);
            detail.setCreateTime(LocalDateTime.now());
            answerDetailMapper.insert(detail);
        }

        // 5. 计算最终得分（假设每道题算一分，或者换算为百分制，这里以纯计个数为例）
        int totalScore = (int) Math.round((double) correctCount / submitDTO.getAnswers().size() * 100);
        record.setScore(totalScore);
        examRecordMapper.updateById(record);

        // 6. 返回结果给前端
        ExamResultVO resultVO = new ExamResultVO();
        resultVO.setRecordId(record.getId());
        resultVO.setScore(totalScore);
        resultVO.setTotal(submitDTO.getAnswers().size());
        return resultVO;
    }
    @Override
    public Long createExam(String title, String startTime, String endTime, int questionCount) {
        // 1. 查询所有单词库的 ID
        List<WordDict> allWords = wordDictMapper.selectList(null);
        if (allWords.size() < questionCount) {
            throw new RuntimeException("题库单词数量不足以生成考试！");
        }

        // 2. 将所有单词在内存中打乱随机抽取前 questionCount 个 ID
        Collections.shuffle(allWords);
        List<Long> selectedIds = allWords.subList(0, questionCount).stream()
                .map(WordDict::getId)
                .collect(Collectors.toList());

        // 3. 构建考试信息写入数据库
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ExamInfo examInfo = new ExamInfo();
        examInfo.setTitle(title);
        examInfo.setStartTime(LocalDateTime.parse(startTime, df));
        examInfo.setEndTime(LocalDateTime.parse(endTime, df));
        examInfo.setQuestionIds(selectedIds);
        examInfo.setCreateTime(LocalDateTime.now());

        examInfoMapper.insert(examInfo);
        return examInfo.getId();
    }

    @Override
    public ExamPaperVO getExamPaper(Long examId) {
        // 1. 获取考试基本信息
        ExamInfo examInfo = examInfoMapper.selectById(examId);
        if (examInfo == null) {
            throw new RuntimeException("考试不存在！");
        }

        // 校验考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(examInfo.getStartTime())) {
            throw new RuntimeException("考试尚未开始！");
        }
        if (now.isAfter(examInfo.getEndTime())) {
            throw new RuntimeException("考试已结束！");
        }

        // 2. 根据记录的 questionIds 查出正确的考题单词列表
        List<Long> questionIds = examInfo.getQuestionIds();
        List<WordDict> targetWords = wordDictMapper.selectBatchIds(questionIds);

        // 3. 为了高效生成干扰项，我们一次性查出系统中所有的单词释义作为“干扰项池”
        // （对于课程设计万级以内的词库，载入内存极其快且减轻数据库 IO 压力）
        List<WordDict> allWords = wordDictMapper.selectList(null);
        List<String> allMeanings = allWords.stream()
                .map(WordDict::getMeaningCn)
                .collect(Collectors.toList());

        List<QuestionVO> questionVOList = new ArrayList<>();
        Random random = new Random();

        // 4. 为每个单词构建选择题（1个正确答案 + 3个干扰答案）
        for (WordDict target : targetWords) {
            QuestionVO vo = new QuestionVO();
            vo.setWordId(target.getId());
            vo.setWordEn(target.getWordEn());

            // 构建选项列表并加入正确答案
            Set<String> optionsSet = new HashSet<>();
            optionsSet.add(target.getMeaningCn());

            // 从池子中随机抽3个不同的干扰项（利用 Set 去重）
            while (optionsSet.size() < 4 && allMeanings.size() >= 4) {
                int randomIndex = random.nextInt(allMeanings.size());
                String randomMeaning = allMeanings.get(randomIndex);
                optionsSet.add(randomMeaning);
            }

            // 将 Set 转为 List 并使用 Collections.shuffle 随机打乱 ABCD 选项顺序！
            List<String> shuffledOptions = new ArrayList<>(optionsSet);
            Collections.shuffle(shuffledOptions);

            vo.setOptions(shuffledOptions);
            questionVOList.add(vo);
        }

        // 5. 【核心需求：题目乱序】
        // 考卷包含的题目内容大家一致，但对每个考生随机打乱题序！
        Collections.shuffle(questionVOList);

        // 6. 组装最终试卷返回
        ExamPaperVO paperVO = new ExamPaperVO();
        paperVO.setExamId(examInfo.getId());
        paperVO.setTitle(examInfo.getTitle());
        paperVO.setStartTime(examInfo.getStartTime());
        paperVO.setEndTime(examInfo.getEndTime());
        paperVO.setQuestions(questionVOList);

        return paperVO;
    }
}